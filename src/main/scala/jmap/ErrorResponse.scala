package jmap

import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class ErrorResponse(`type`: String, description: Option[String])

object ErrorResponse {
  implicit val decoder: JsonDecoder[ErrorResponse] = DeriveJsonDecoder.gen[ErrorResponse]
}
