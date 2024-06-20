package com.rogervinas.sample.api.client

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class SampleThing(
  val name: String,
  val value: Double,
  @JsonFormat(pattern = "yyyy-MM-dd") val date: LocalDate,
)

data class SampleThingId(
  val id: Int,
)

interface SampleApiClient {
  suspend fun create(thing: SampleThing): SampleThingId?

  suspend fun get(thingId: SampleThingId): SampleThing?
}
