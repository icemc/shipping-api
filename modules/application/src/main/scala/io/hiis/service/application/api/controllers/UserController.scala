package io.hiis.service.application.api.controllers

import io.circe.syntax.EncoderOps
import io.hiis.service.application.api.utils.Api.ApiError.{ notFound, NotFound }
import io.hiis.service.application.api.utils.Controller
import io.hiis.service.application.services.UserService
import io.hiis.service.application.services.security.AuthTokenService
import io.hiis.service.core.models.auth.SecuredRequest
import io.hiis.service.core.utils.Logging
import sttp.tapir.ztapir._
import zio.ZIO

/** Created by Abanda Ludovic on 31/10/2022 */

final case class UserController(userService: UserService)(implicit
    authTokenService: AuthTokenService
) extends Controller
    with Logging {

  private val getUSer: ZServerEndpoint[Any, Any] = SecuredEndpoint(notFound).get
    .in(BaseUrl / "auth" / "user")
    .out(stringBody)
    .serverLogic { implicit request: SecuredRequest => _: Unit =>
      for {
        user <- userService.get(request.identity.id).flatMap {
          case Some(value) => ZIO.succeed(value)
          case None        => ZIO.fail(NotFound("User not found"))
        }
      } yield user.copy(lastLoginAt = None, createdByIp = None, createdAt = None).asJson.noSpaces
    }

  override def endpoints: List[_root_.sttp.tapir.ztapir.ZServerEndpoint[Any, Any]] =
    List(getUSer)
}
