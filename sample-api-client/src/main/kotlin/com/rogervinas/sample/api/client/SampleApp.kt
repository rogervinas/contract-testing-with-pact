package com.rogervinas.sample.api.client

import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

suspend fun main() {
  SampleApiKtorClient("http://localhost:8080")
    .create(SampleThing(UUID.randomUUID().toString(), Random.nextDouble(), LocalDate.now()))
    ?.let { thingId ->
      val thing = SampleApiKtorClient("http://localhost:8080").get(thingId)
      println("Created $thing with $thingId")
    }
}
