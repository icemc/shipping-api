package io.hiis.service.core.models

/** Created by Abanda Ludovic on 17/01/2023 */

object Config {
  final case class MongodbConfig(uri: String, database: String)

  final case class AuthConfig(
      key: String,
      authTokenHeader: String,
      authTokenMaxAge: Long,
      refreshTokenHeader: String
  )

  abstract class ExternalServiceConfig(val name: String, val host: String)

  type AllConfig = MongodbConfig with AuthConfig
}
