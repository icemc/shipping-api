package io.hiis.service.application.api.controllers

import io.circe.Json
import io.circe.generic.auto.{ exportDecoder, exportEncoder }
import io.circe.syntax.EncoderOps
import io.hiis.service.application.api.utils.Api.ApiError.{
  forbidden,
  Forbidden,
  InternalServerError
}
import io.hiis.service.application.api.utils.Controller
import io.hiis.service.application.models.Credentials
import io.hiis.service.application.services.UserService
import io.hiis.service.application.services.security.{ AuthTokenService, PasswordService }
import io.hiis.service.core.models.auth.UnsecuredRequest
import io.hiis.service.core.utils.Logging
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._
import zio.ZIO
import zio.http.model.HttpError.BadRequest

import java.time.Instant

/** Created by Abanda Ludovic on 27/10/2022 */

final case class SigninController(
    userService: UserService,
    passwordService: PasswordService,
    authTokenService: AuthTokenService
) extends Controller
    with Logging {

  private val login: ZServerEndpoint[Any, Any] = UnsecuredEndpoint(forbidden).post
    .in(BaseUrl / "auth" / "signin")
    .in(jsonBody[Credentials])
    .out(stringBody)
    .serverLogic { implicit request: UnsecuredRequest => credentials: Credentials =>
      for {
        maybeUser <- userService
          .getByIdentifier(credentials.identifier)
          .mapError(_ => BadRequest("Error with credentials"))
        result <- maybeUser
          .map { user =>
            if (!user.isActivated) {
              ZIO.fail(Forbidden("Account not verified"))
            } else {
              passwordService
                .validate(user.id, credentials.password)
                .flatMap { isAuth =>
                  if (isAuth) {
                    for {
                      jwt <- authTokenService.create(user.id)
                      _   <- userService.update(user.copy(lastLoginAt = Some(Instant.now())))
                    } yield Json
                      .obj(
                        "user" -> user
                          .copy(lastLoginAt = None, createdByIp = None, createdAt = None)
                          .asJson,
                        "token" -> jwt.asJson
                      )
                      .noSpaces
                  } else {
                    ZIO.fail(BadRequest("Error with credentials"))
                  }
                }
            }
          }
          .getOrElse(ZIO.fail(BadRequest("Error with credentials")))
      } yield result
    }

  override def endpoints = List(login)
}
