package io.hiis.service.application.api.controllers

import io.circe.Json
import io.circe.generic.auto.{ exportDecoder, exportEncoder }
import io.hiis.service.application.api.utils.Controller
import io.hiis.service.application.models.Password
import io.hiis.service.application.services.UserService
import io.hiis.service.application.services.security.{ AuthTokenService, PasswordService }
import io.hiis.service.core.models.auth.SecuredRequest
import io.hiis.service.core.utils.Logging
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._

/** Created by Abanda Ludovic on 28/10/2022 */

final case class PasswordController(
    userService: UserService,
    passwordService: PasswordService
)(implicit authTokenService: AuthTokenService)
    extends Controller
    with Logging {

  private val resetPassword: ZServerEndpoint[Any, Any] = SecuredEndpoint().post
    .in(BaseUrl / "auth" / "change-password")
    .in(jsonBody[Password])
    .out(stringBody)
    .serverLogic { implicit request: SecuredRequest => password: Password =>
      for {
        _ <- passwordService
          .update(request.identity.id, password.password)
      } yield Json.obj("message" -> Json.fromString("Password updated successfully")).noSpaces
    }

  override def endpoints: List[_root_.sttp.tapir.ztapir.ZServerEndpoint[Any, Any]] =
    List(resetPassword)
}
