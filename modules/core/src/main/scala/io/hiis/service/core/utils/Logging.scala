package io.hiis.service.core.utils

import io.hiis.service.core.models.auth.Request
import zio.logging.backend.SLF4J
import zio.{ Runtime, ZIO }

/** Created by Abanda Ludovic on 17/10/2022 */

trait Logging {
  final protected val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  protected val loggerName: String = s"${this.getClass.getName}"

  private def loggerValue = SLF4J.loggerName(loggerName)

  final protected def logInfo(message: String)(implicit
      request: Request = Request.DUMMY_REQUEST
  ): ZIO[Any, Nothing, Unit] =
    (ZIO.logInfo(s"RequestId: ${request.requestId.id}, message: $message") @@ loggerValue)
      .provide(logger)

  final protected def logWarning(message: String)(implicit
      request: Request = Request.DUMMY_REQUEST
  ): ZIO[Any, Nothing, Unit] =
    (ZIO.logWarning(s"RequestId: ${request.requestId.id}, message: $message") @@ loggerValue)
      .provide(logger)

  final protected def logError(message: String)(implicit
      cause: Throwable,
      request: Request = Request.DUMMY_REQUEST
  ): ZIO[Any, Nothing, Unit] = for {
    _ <- (ZIO.logError(s"RequestId: ${request.requestId.id}, message: $message") @@ loggerValue)
      .provide(logger)
    _ <- ZIO.succeed(cause.printStackTrace())
  } yield ()

  final protected def logDebug(message: String)(implicit
      request: Request = Request.DUMMY_REQUEST
  ): ZIO[Any, Nothing, Unit] =
    (ZIO.logDebug(s"RequestId: ${request.requestId.id}, message: $message") @@ loggerValue)
      .provide(logger)
}
