package io.hiis.service.core.models.auth

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

import java.time.Instant

/** Created by Abanda Ludovic on 26/10/2022 */

final case class PasswordInfo(
    id: String,
    hashes: List[String],
    lastUpdated: Instant
)

object PasswordInfo {

  import io.hiis.service.core.utils.ImplicitJsonFormats.InstantFormat._

  implicit val encoder: Encoder[PasswordInfo] = deriveEncoder
  implicit val decoder: Decoder[PasswordInfo] = deriveDecoder
}
