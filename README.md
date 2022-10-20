[![CI](https://github.com/rogervinas/contract-testing-with-pact/actions/workflows/ci.yml/badge.svg)](https://github.com/rogervinas/contract-testing-with-pact/actions/workflows/ci.yml)

# Contract Testing with Pact

This PoC shows a step by step implementation of contract testing using [Pact](https://docs.pact.io/).

First of all a couple of definitions:

> **[Contract testing](https://docs.pact.io/#what-is-contract-testing)** is a technique for testing an integration point by checking each application in isolation to ensure the messages it sends or receives conform to a shared understanding that is documented in a "contract".
> 
> **[Consumer driven contract testing](https://pactflow.io/what-is-consumer-driven-contract-testing/)** is a type of **contract testing** that ensures that a provider is compatible with the expectations that the consumer has of it. For an HTTP API (and other synchronous protocols), this would involve checking that the provider accepts the expected requests, and that it returns the expected responses. For a system that uses message queues, this would involve checking that the provider generates the expected message.

So let's try to implement this flow:

![ContractTesting](doc/ContractTesting.png)

* [1) Consumer defines the "contract" with the Provider](#1-consumer-defines-the-contract-with-the-provider)
* [2) Consumer tests the "contract" using a provider mock](#2-consumer-tests-the-contract-using-a-provider-mock)
* [3) Consumer publishes the "contract"](#3-consumer-publishes-the-contract)
* [4) Provider tests the "contract" using a consumer mock](#4-provider-tests-the-contract-using-a-consumer-mock)
* [5) Provider verifies or refutes the "contract" publishing the results of the test](#5-provider-verifies-or-refutes-the-contract-publishing-the-results-of-the-test)
* [6) Consumer should only be deployed if the "contract" is verified](#6-consumer-should-only-be-deployed-if-the-contract-is-verified)
* [7) Provider should only be deployed if the "contract" is verified](#7-provider-should-only-be-deployed-if-the-contract-is-verified)
* [Implementation Details](#implementation-details)

## 1) Consumer defines the "contract" with the Provider

For the "Sample API Client" we will use [Kotlin](https://kotlinlang.org/) and [Ktor client](https://ktor.io/docs/create-client.html).

To define the `POST /thing` endpoint we specify the following "pact":

```kotlin
@ExtendWith(PactConsumerTestExt::class)
@TestInstance(PER_CLASS)
class SampleApiClientContractTest {
  @Pact(provider = "Sample API Server", consumer = "Sample API Client")
  fun create(builder: PactDslWithProvider): V4Pact {
    return builder
      .given("Initial State")
      .uponReceiving("Create Thing")
      .path("/thing")
      .method("POST")
      .headers(mapOf("Content-Type" to "application/json"))
      .body(
        """
        {
          "name": "Foo",
          "value": 123.45,
          "date": "2022-10-13"
        }
        """.trimIndent()
      )
      .willRespondWith()
      .status(201)
      .headers(mapOf("Content-Type" to "application/json"))
      .body(
        """
        {
          "id": 123
        }
        """.trimIndent()
      )
      .toPact(V4Pact::class.java)
  }
}
```

For simplicity we use fixed JSON expectations but in a real example you may use [PactDslJsonBody DSL](https://docs.pact.io/implementation_guides/jvm/consumer#building-json-bodies-with-pactdsljsonbody-dsl) which allows to specify regex and type matchers to each field:

For example the request can be specified as:
```kotlin 
  .body(PactDslJsonBody()
    .stringMatcher("name", "\\w+", "Foo")
    .decimalType("value", 123.45)
    .localDate("date", "yyyy-MM-dd", LocalDate.of(2022, 10, 13))
  )
```

And the response:
```kotlin
  .body(PactDslJsonBody()
    .integerType("id", 123)
  )
```

To define the  `GET /thing/{id}` endpoint we will specify two "pacts":

* One for the case "thing exists":
```kotlin
@Pact(provider = "Sample API Server", consumer = "Sample API Client")
fun getExistingThing(builder: PactDslWithProvider): V4Pact {
return builder
  .given("Thing 123 exists")
  .uponReceiving("Get Thing 123 when it exists")
  .path("/thing/123")
  .method("GET")
  .willRespondWith()
  .status(200)
  .headers(mapOf("Content-Type" to "application/json"))
  .body(
    """
      {
        "name": "Foo",
        "value": 123.45,
        "date": "2022-10-13"
      }
      """.trimIndent()
  )
  .toPact(V4Pact::class.java)
}
```

* Another one for the case "thing does not exist":
```kotlin
@Pact(provider = "Sample API Server", consumer = "Sample API Client")
fun getNonExistingThing(builder: PactDslWithProvider): V4Pact {
return builder
  .given("Initial State")
  .uponReceiving("Get Thing 123 when it does not exist")
  .path("/thing/123")
  .method("GET")
  .willRespondWith()
  .status(404)
  .toPact(V4Pact::class.java)
}
```

## 2) Consumer tests the "contract" using a provider mock

Now we create tests for the three pacts defined in the previous step:

```kotlin
@Test
@PactTestFor(providerName = "Sample API Server", pactMethod = "create", providerType = SYNCH)
fun `should create thing`(mockServer: MockServer) {
  val client = SampleApiKtorClient(mockServer.getUrl())
  val thingId = runBlocking {
    client.create(SampleThing("Foo", 123.45, LocalDate.of(2022, 10, 13)))
  }
  assertThat(thingId)
    .isEqualTo(SampleThingId(123))
}

@Test
@PactTestFor(providerName = "Sample API Server", pactMethod = "getExistingThing", providerType = SYNCH)
fun `should get thing 123 when it exists`(mockServer: MockServer) {
  val client = SampleApiKtorClient(mockServer.getUrl())
  val thing = runBlocking { 
    client.get(SampleThingId(123)) 
  }
  assertThat(thing)
    .isEqualTo(SampleThing("Foo", 123.45, LocalDate.of(2022, 10, 13)))
}

@Test
@PactTestFor(providerName = "Sample API Server", pactMethod = "getNonExistingThing", providerType = SYNCH)
fun `should not get thing 123 when it does not exist`(mockServer: MockServer) { 
  val client = SampleApiKtorClient(mockServer.getUrl())
  val thing = runBlocking {
    client.get(SampleThingId(123))
  }
  assertThat(thing).isNull()
}
```

Note that:
* `pactMethod` should match the name of the method annotated with `@Pact`.
* Just for documentation, we specify the provider as a synchronous provider (HTTP)

If now we execute tests on `SampleApiClientContractTest`:
1. Tests will be executed against a provider mock.
2. The "contract" will be generated locally under `build/pacts`. We can generate them in another directory using `@PactDirectory` annotation or `pact.rootDir` system property.

You can check the "contract" in `build/pacts/Sample API Client-Sample API Server.json` file.

## 3) Consumer publishes the "contract"

The consumer "contract" is now generated locally, but it should be published to a [Pact Broker](https://docs.pact.io/pact_broker), so it can be shared with the provider.

1. Just for this PoC we will use docker compose to start a local instance of [Pact Broker](https://docs.pact.io/pact_broker) with a sqlite database:
```shell
docker compose up -d
```

2. We can go to http://localhost:9292, and we will see a "contract" example that comes by default:
![PactBroker-1](doc/PactBroker-1.png)

3. We publish our consumer "contract" using pact gradle plugin:
```shell
cd ./sample-api-client
./gradlew pactPublish

> Task :pactPublish
Publishing 'Sample API Client-Sample API Server.json' ... 
OK
```

4. We go back to http://localhost:9292, and we will see our consumer "contract":
![PactBroker-2](doc/PactBroker-2.gif)

## 4) Provider tests the "contract" using a consumer mock



## 5) Provider verifies or refutes the "contract" publishing the results of the test

TODO

## 6) Consumer should only be deployed if the "contract" is verified

TODO

## 7) Provider should only be deployed if the "contract" is verified

TODO

## Implementation Details

* TODO Pact gradle plugin id("au.com.dius.pact") version "4.3.15"
  * TODO properties set for the consumer
  * TODO properties set for the provider
* TODO Pact dependencies for the consumer au.com.dius.pact.consumer:junit5:4.3.15
* TODO Pact dependencies for the provider au.com.dius.pact.provider:junit5spring:4.3.15