package io.hiis.service.application.api.utils

import io.hiis.service.application.api.utils.Api.ApiError.InternalServerError
import io.hiis.service.core.utils.Logging
import zio.CanFail.canFailAmbiguous1
import zio.{ IO, ZIO }

/** Created by Abanda Ludovic on 19/01/2023 */

trait ApiTypes { self: Logging =>

  type ApiTask[T] = IO[InternalServerError, T]

  implicit def taskToApiTask[T](task: ZIO[Any, Throwable, T]): ApiTask[T] = task.flatMapError {
    implicit error =>
      self.logError(
        s"Failed to perform operation, error: ${error.getMessage}"
      ) *>
        ZIO.succeed(InternalServerError())
  }

  implicit def UioToApiTask[T](task: ZIO[Any, Nothing, T]): ApiTask[T] = task.flatMapError {
    implicit error =>
      self.logError(
        s"Failed to perform operation"
      ) *>
        ZIO.succeed(InternalServerError())
  }

}
