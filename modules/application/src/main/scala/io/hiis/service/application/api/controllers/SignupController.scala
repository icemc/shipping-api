package io.hiis.service.application.api.controllers

import io.circe.Json
import io.circe.generic.auto.{ exportDecoder, exportEncoder }
import io.hiis.service.application.api.utils.Api.ApiError.{ conflict, Conflict }
import io.hiis.service.application.api.utils.Controller
import io.hiis.service.application.models.Signup
import io.hiis.service.application.services.UserService
import io.hiis.service.application.services.security.PasswordService
import io.hiis.service.core.models.auth.{ UnsecuredRequest, User }
import io.hiis.service.core.utils.Logging
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._
import zio.ZIO

import java.util.UUID

/** Created by Abanda Ludovic on 27/10/2022 */

final case class SignupController(
    userService: UserService,
    passwordService: PasswordService
) extends Controller
    with Logging {

  private val signup: ZServerEndpoint[Any, Any] = UnsecuredEndpoint(conflict).post
    .in(BaseUrl / "auth" / "signup")
    .in(jsonBody[Signup])
    .out(stringBody)
    .serverLogic { implicit request: UnsecuredRequest => credentials: Signup =>
      userService
        .isIdentifierUsed(credentials.identifier)
        .flatMap {
          case false =>
            for {
              user <- userService.save(
                User(
                  id = UUID.randomUUID().toString.replace("-", ""),
                  identifier = credentials.identifier,
                  firstName = credentials.firstName,
                  lastName = credentials.lastName,
                  email = Some(credentials.email)
                )
              )
              _ <- passwordService.create(user.id, credentials.password)
            } yield Json.obj("message" -> Json.fromString("User created")).noSpaces
          case true => ZIO.fail(Conflict("Identifier already in use"))
        }
    }

  override def endpoints = List(signup)
}
