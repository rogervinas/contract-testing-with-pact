package com.rogervinas.sample.api.client

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson

class SampleApiKtorClient(private val serverUrl: String) : SampleApiClient {

  private val client = HttpClient(CIO) {
    install(ContentNegotiation) {
      jackson {
        registerModule(JavaTimeModule())
      }
    }
  }

  override suspend fun create(thing: SampleThing): SampleThingId? {
    val response = client.post("$serverUrl/thing") {
      contentType(ContentType.Application.Json)
      setBody(thing)
    }
    return when(response.status) {
      HttpStatusCode.Created -> response.body<SampleThingId>()
      else -> null
    }
  }

  override suspend fun get(thingId: SampleThingId): SampleThing? {
    val response = client.get("$serverUrl/thing/${thingId.id}")
    return when(response.status) {
      HttpStatusCode.OK -> response.body<SampleThing>()
      else -> null
    }
  }
}
