package com.rogervinas.sample.api.server

import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import java.time.LocalDate


@SpringBootTest(webEnvironment = RANDOM_PORT)
@Provider("Sample API Server")
@PactBroker
@ExtendWith(PactVerificationSpringProvider::class)
class SampleApiServerContractTest {

  @Autowired
  private lateinit var repository: SampleRepository

  @LocalServerPort
  private var port = 0

  @BeforeEach
  fun beforeEach(context: PactVerificationContext) {
    context.target = HttpTestTarget("localhost", port)
  }

  @TestTemplate
  fun pactVerificationTestTemplate(context: PactVerificationContext) {
    context.verifyInteraction()
  }

  @State("Initial State")
  fun `initial state`() {
    repository.reset(123)
  }

  @State("Thing 123 exists")
  fun `thing 123 exists`() {
    repository.reset(123)
    repository.save(SampleThing("Foo", 123.45, LocalDate.of(2022, 10, 13)))
  }
}
