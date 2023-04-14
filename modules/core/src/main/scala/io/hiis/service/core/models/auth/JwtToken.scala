package io.hiis.service.core.models.auth

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

import java.time.Instant

case class JwtToken(token: String, expiresOn: Instant)

object JwtToken {
  import io.hiis.service.core.utils.ImplicitJsonFormats.InstantFormat._

  implicit val encoder: Encoder[JwtToken] = deriveEncoder
  implicit val decoder: Decoder[JwtToken] = deriveDecoder
}
