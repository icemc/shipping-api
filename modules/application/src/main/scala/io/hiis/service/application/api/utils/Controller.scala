package io.hiis.service.application.api.utils

import io.hiis.service.core.utils.Logging
import sttp.tapir.EndpointInput
import sttp.tapir.ztapir._

/** Created by Abanda Ludovic on 19/01/2023 */

private[api] trait Controller extends Api { self: Logging =>
  protected def BaseUrl: EndpointInput[Unit] = "api"

  def endpoints: List[ZServerEndpoint[Any, Any]]
}
