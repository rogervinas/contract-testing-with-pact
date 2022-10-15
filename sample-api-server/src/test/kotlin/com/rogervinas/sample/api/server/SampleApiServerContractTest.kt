package com.rogervinas.sample.api.server

import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Provider("Sample API Server")
@PactBroker
class SampleApiServerContractTest {

  @Autowired
  private lateinit var repository: SampleRepository

  @TestTemplate
  @ExtendWith(PactVerificationSpringProvider::class)
  fun pactVerificationTestTemplate(context: PactVerificationContext) {
    context.verifyInteraction()
  }

  @State("Initial State")
  fun `initial state`() {
    repository.reset(123)
  }

  @State("Thing 123 exists")
  fun `thing 123 exists`() {
    repository.reset(125)
    repository.save(SampleThing("Foo", 123.45, LocalDate.of(2022, 10, 13)))
  }
}
