package com.rogervinas.sample.api.server

import org.springframework.stereotype.Repository
import java.util.concurrent.atomic.AtomicInteger

@Repository
class SampleRepository {
  private val sampleThingIdNext = AtomicInteger(1)
  private val sampleThings = mutableMapOf<SampleThingId, SampleThing>()

  fun save(thing: SampleThing): SampleThingId {
    val thingId = SampleThingId(sampleThingIdNext.getAndIncrement())
    sampleThings[thingId] = thing
    return thingId
  }

  fun get(thingId: SampleThingId) = sampleThings[thingId]

  fun reset(nextId: Int) {
    sampleThingIdNext.set(nextId)
    sampleThings.clear()
  }
}
