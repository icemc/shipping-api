package io.hiis.service.application.api

import io.hiis.service.application.api.controllers._
import io.hiis.service.application.api.utils.Controller
import io.hiis.service.application.services.security.{ AuthTokenService, PasswordService }
import io.hiis.service.application.services.{ ShipmentService, UserService }
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.http.Server
import zio.{ ZIO, ZLayer }

/** Created by Abanda Ludovic on 19/01/2023 */

final case class ApiGateway(routes: Controller*) {
  val allEndpoints: List[ZServerEndpoint[Any, Any]] =
    routes.flatMap(_.endpoints).toList

  // Docs
  private val swaggerEndpoints =
    SwaggerInterpreter().fromServerEndpoints(allEndpoints, "The App Name Server", "1.0")

  private val APP =
    ZioHttpInterpreter().toHttp(allEndpoints ++ swaggerEndpoints)

  def start = for {
    _ <- Server.serve(APP).provide(Server.default)
  } yield ()
}

object ApiGateway {
  // Compose your ApiGateway layer here
  val live: ZLayer[
    UserService with AuthTokenService with ShipmentService with PasswordService,
    Nothing,
    ApiGateway
  ] = ZLayer.fromZIO(
    for {
      passwordService  <- ZIO.service[PasswordService]
      shipmentService  <- ZIO.service[ShipmentService]
      authTokenService <- ZIO.service[AuthTokenService]
      userService      <- ZIO.service[UserService]
    } yield ApiGateway(
      PasswordController(userService, passwordService)(authTokenService),
      ShipmentController(shipmentService)(authTokenService),
      SigninController(userService, passwordService, authTokenService),
      SignupController(userService, passwordService),
      UserController(userService)(authTokenService),
      HealthController
    )
  )
}
