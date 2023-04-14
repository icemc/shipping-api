package io.hiis.service.application.api.controllers

import io.hiis.service.application.api.utils.Api.ApiError.InternalServerError
import io.hiis.service.application.api.utils.Controller
import io.hiis.service.core.utils.Logging
import sttp.tapir.ztapir._
import zio.CanFail.canFailAmbiguous1
import zio.ZIO

/** Created by Abanda Ludovic on 19/01/2023 */

object HealthController extends Controller with Logging {
  val healthEndpoint: ZServerEndpoint[Any, Any] = SimpleEndpoint().get
    .in(BaseUrl / "health")
    .out(stringBody)
    .zServerLogic(_ =>
      logInfo("Checking health status") *> ZIO
        .succeed("OK")
        .mapError(_ => InternalServerError())
    )

  override def endpoints: List[ZServerEndpoint[Any, Any]] = List(healthEndpoint)
}
