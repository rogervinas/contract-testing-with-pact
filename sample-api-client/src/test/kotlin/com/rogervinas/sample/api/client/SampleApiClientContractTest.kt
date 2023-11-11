package com.rogervinas.sample.api.client

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.consumer.junit5.ProviderType.SYNCH
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate


@ExtendWith(PactConsumerTestExt::class)
@TestInstance(PER_CLASS)
class SampleApiClientContractTest {

  companion object {
    private val THING123 = SampleThing("Foo", 123.45, LocalDate.of(2022, 10, 13))
    private val THING123_JSON_PACT = PactDslJsonBody()
      .stringMatcher("name", "\\w+", "Foo")
      .decimalType("value", 123.45)
      .localDate("date", "yyyy-MM-dd", LocalDate.of(2022, 10, 13))

    private val THING123_ID = SampleThingId(123)
    private val THING123_ID_JSON_PACT = PactDslJsonBody()
      .integerType("id", 123)
  }

  @Pact(provider = "Sample API Server", consumer = "Sample API Client")
  fun create(builder: PactDslWithProvider): V4Pact {
    return builder
      .given("Initial State")
      .uponReceiving("Create Thing")
      .path("/thing")
      .method("POST")
      .headers(mapOf("Content-Type" to "application/json"))
      .body(THING123_JSON_PACT)
      .willRespondWith()
      .status(201)
      .headers(mapOf("Content-Type" to "application/json"))
      .body(THING123_ID_JSON_PACT)
      .toPact(V4Pact::class.java)
  }

  @Test
  @PactTestFor(providerName = "Sample API Server", pactMethod = "create", providerType = SYNCH)
  fun `should create thing`(mockServer: MockServer) {
    val client = SampleApiKtorClient(mockServer.getUrl())
    val thingId = runBlocking {
      client.create(THING123)
    }
    assertThat(thingId)
      .isEqualTo(THING123_ID)
  }

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
      .body(THING123_JSON_PACT)
      .toPact(V4Pact::class.java)
  }

  @Test
  @PactTestFor(providerName = "Sample API Server", pactMethod = "getExistingThing", providerType = SYNCH)
  fun `should get thing 123 when it exists`(mockServer: MockServer) {
    val client = SampleApiKtorClient(mockServer.getUrl())
    val thing = runBlocking {
      client.get(THING123_ID)
    }
    assertThat(thing)
      .isEqualTo(THING123)
  }

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

  @Test
  @PactTestFor(providerName = "Sample API Server", pactMethod = "getNonExistingThing", providerType = SYNCH)
  fun `should not get thing 123 when it does not exist`(mockServer: MockServer) {
    val client = SampleApiKtorClient(mockServer.getUrl())
    val thing = runBlocking {
      client.get(THING123_ID)
    }
    assertThat(thing).isNull()
  }
}
