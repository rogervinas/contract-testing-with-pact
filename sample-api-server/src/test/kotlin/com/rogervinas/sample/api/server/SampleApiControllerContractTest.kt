package com.rogervinas.sample.api.server

import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import au.com.dius.pact.provider.spring.junit5.WebTestClientTarget
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDate

@WebFluxTest(controllers = [SampleApiController::class])
@Provider("Sample API Server")
@PactBroker
@ExtendWith(PactVerificationSpringProvider::class)
class SampleApiControllerContractTest {

  @Autowired
  private lateinit var webTestClient: WebTestClient

  @MockkBean
  private lateinit var repository: SampleRepository

  @BeforeEach
  fun beforeEach(context: PactVerificationContext) {
    context.target = WebTestClientTarget(webTestClient)
  }

  @TestTemplate
  fun pactVerificationTestTemplate(context: PactVerificationContext) {
    context.verifyInteraction()
  }

  @State("Initial State")
  fun `initial state`() {
    every { repository.save(any()) } returns SampleThingId(123)
    every { repository.get(any()) } returns null
  }

  @State("Thing 123 exists")
  fun `thing 123 exists`() {
    every { repository.get(any()) } returns SampleThing("Foo", 123.45, LocalDate.of(2022, 10, 13))
  }
}
