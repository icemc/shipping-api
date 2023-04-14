package io.hiis.service.core.models.auth

/** Created by Abanda Ludovic on 17/01/2023 */

final case class Identity(id: String) extends AnyVal

object Identity {
  implicit def stringToUserId(value: String): Identity = Identity(value)
}
