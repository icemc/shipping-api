package io.hiis.service.core.models.auth

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

import java.time.Instant

case class User(
    id: String,
    identifier: String,
    firstName: String,
    lastName: String,
    avatarURL: Option[String] = None,
    email: Option[String] = None,
    isActivated: Boolean = false,
    lastLoginAt: Option[Instant] = None,
    createdByIp: Option[String] = None,
    createdAt: Option[Instant] = None
) extends Serializable

object User {
  import io.hiis.service.core.utils.ImplicitJsonFormats.InstantFormat._

  implicit val encoder: Encoder[User] = deriveEncoder
  implicit val decoder: Decoder[User] = deriveDecoder
}
