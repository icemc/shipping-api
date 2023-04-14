package io.hiis.service.application

import io.hiis.service.core.models.Config._
import zio.config.ReadError
import zio.config.magnolia.descriptor
import zio.config.syntax.ZIOConfigNarrowOps
import zio.config.typesafe.TypesafeConfig
import zio.{ Layer, TaskLayer }

object AppConfig {
  final case class ConfigDescriptor(
      auth: AuthConfig,
      mongodb: MongodbConfig
  )

  val appConfig: Layer[ReadError[String], ConfigDescriptor] =
    TypesafeConfig.fromResourcePath(descriptor[ConfigDescriptor])

  val live: TaskLayer[AllConfig] =
    appConfig.narrow(_.mongodb) >+>
      appConfig.narrow(_.auth)
}
