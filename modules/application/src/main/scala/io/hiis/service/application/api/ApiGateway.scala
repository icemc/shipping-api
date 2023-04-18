package io.hiis.service.application.api

import io.hiis.service.application.api.controllers._
import io.hiis.service.application.api.utils.Controller
import io.hiis.service.application.services.security.{AuthTokenService, PasswordService}
import io.hiis.service.application.services.{ShipmentService, UserService}
import sttp.model.{Method, StatusCode}
import sttp.tapir.server.interceptor.cors.CORSConfig.{AllowedCredentials, AllowedHeaders, AllowedMethods, AllowedOrigin, ExposedHeaders, MaxAge}
import sttp.tapir.server.interceptor.cors.{CORSConfig, CORSInterceptor}
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.http.Server
import zio.{Task, ZIO, ZLayer}

/** Created by Abanda Ludovic on 19/01/2023 */

final case class ApiGateway(routes: Controller*) {
  val allEndpoints: List[ZServerEndpoint[Any, Any]] =
    routes.flatMap(_.endpoints).toList

  // Docs
  private val swaggerEndpoints =
    SwaggerInterpreter().fromServerEndpoints(allEndpoints, "The App Name Server", "1.0")

  val cors: CORSInterceptor[Task] = CORSInterceptor.customOrThrow[Task](
    CORSConfig(
      allowedOrigin = AllowedOrigin.All,
      allowedCredentials = AllowedCredentials.Deny,
      allowedMethods = AllowedMethods.All,
      allowedHeaders = AllowedHeaders.All,
      exposedHeaders = ExposedHeaders.None,
      maxAge = MaxAge.Default,
      preflightResponseStatusCode = StatusCode.NoContent
    )
  )

  private val APP =
    ZioHttpInterpreter[Any](ZioHttpServerOptions.customiseInterceptors.appendInterceptor(cors).options).toHttp(allEndpoints ++ swaggerEndpoints)

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
      passwordService <- ZIO.service[PasswordService]
      shipmentService <- ZIO.service[ShipmentService]
      authTokenService <- ZIO.service[AuthTokenService]
      userService <- ZIO.service[UserService]
    } yield ApiGateway(
      PasswordController(userService, passwordService)(authTokenService),
      ShipmentController(shipmentService)(authTokenService),
      SigninController(userService, passwordService, authTokenService),
      SignupController(userService, passwordService),
      UserController(userService)(authTokenService),
      HealthController,
      OptionsController
    )
  )
}
