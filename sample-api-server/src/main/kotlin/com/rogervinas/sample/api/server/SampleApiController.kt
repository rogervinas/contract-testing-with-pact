package com.rogervinas.sample.api.server

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

data class SampleThing(
  val name: String?,
  val value: Double?,
  @JsonFormat(pattern = "yyyy-MM-dd") val date: LocalDate?
)

data class SampleThingId(
  val id: Int
)

@RestController
class SampleApiController(private val repository: SampleRepository) {

  @PostMapping("/thing", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
  @ResponseStatus(CREATED)
  suspend fun create(@RequestBody thing: SampleThing) = repository.save(thing)

  @GetMapping("/thing/{id}", produces = [APPLICATION_JSON_VALUE])
  suspend fun get(@PathVariable("id") id: Int) =
    when (val thing = repository.get(SampleThingId(id))) {
      null -> ResponseEntity.notFound().build()
      else -> ResponseEntity.ok(thing)
    }
}
