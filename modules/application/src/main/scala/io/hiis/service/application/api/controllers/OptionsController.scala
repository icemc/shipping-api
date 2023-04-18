package io.hiis.service.application.api.controllers

import io.hiis.service.application.api.utils.Api.ApiError.InternalServerError
import io.hiis.service.application.api.utils.Controller
import io.hiis.service.core.utils.Logging
import sttp.tapir.ztapir._
import zio.CanFail.canFailAmbiguous1
import zio.ZIO

case object OptionsController extends Controller with Logging {
  val optionsEndpoint: ZServerEndpoint[Any, Any] = SimpleEndpoint().options
    .in(BaseUrl / paths)
    .out(stringBody)
    .zServerLogic(_ =>
      logInfo("Options call") *> ZIO
        .succeed("OK")
        .mapError(_ => InternalServerError())
    )

  override def endpoints: List[ZServerEndpoint[Any, Any]] = List(optionsEndpoint)
}