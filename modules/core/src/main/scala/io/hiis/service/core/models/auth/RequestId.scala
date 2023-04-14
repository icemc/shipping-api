package io.hiis.service.core.models.auth

/** Created by Abanda Ludovic on 17/01/2023 */

final case class RequestId(id: String) extends AnyVal

object RequestId {
  val DUMMY_REQUEST_ID = "N/A"

  implicit def StringToRequest(value: String): RequestId = RequestId(value)

  implicit class ToRequest(value: String) {
    def toRequestId: RequestId =
      if (value.nonEmpty) value
      else DUMMY_REQUEST_ID
  }
}
