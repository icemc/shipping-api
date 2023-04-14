package io.hiis.service.application

import io.hiis.service.application.api.ApiGateway
import io.hiis.service.application.services.{ ShipmentService, UserService }
import io.hiis.service.application.services.security.{ AuthTokenService, PasswordService }
import io.hiis.service.core.models.Config.{ AuthConfig, MongodbConfig }
import io.hiis.service.core.utils.Logging
import mongo4cats.zio.ZMongoClient
import zio.{ Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer }

object Application extends ZIOAppDefault with Logging {

  val mongodbClient: ZLayer[Any, Throwable, ZMongoClient] = AppConfig.appConfig.flatMap(layer =>
    ZLayer.scoped[Any](ZMongoClient.fromConnectionString(layer.get.mongodb.uri))
  )

  val app = for {
    mongodbConfig <- ZIO.service[MongodbConfig]
    authConfig    <- ZIO.service[AuthConfig]
    _ <- (ZIO
      .service[ApiGateway]
      .flatMap(_.start) <& logInfo("Started API Gateway Server"))
      .provide(
        ApiGateway.live,
        UserService.live,
        PasswordService.live,
        AuthTokenService.live,
        ShipmentService.live,
        mongodbClient,
        ZLayer.succeed(authConfig),
        ZLayer.succeed(mongodbConfig)
      )
  } yield ()

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = app.provide(AppConfig.live)
}
