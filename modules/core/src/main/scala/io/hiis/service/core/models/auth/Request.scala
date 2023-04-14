package io.hiis.service.core.models.auth

import sttp.model.Header

sealed trait Request {
  def requestId: RequestId = RequestId.DUMMY_REQUEST_ID

  def headers: List[Header] = List.empty
}

object Request {
  val DUMMY_REQUEST: Request = new Request {}
}

final case class SecuredRequest(
    identity: Identity,
    override val requestId: RequestId,
    override val headers: List[Header]
) extends Request

final case class UnsecuredRequest(
    override val requestId: RequestId,
    override val headers: List[Header]
) extends Request

final case class UserAwareRequest(
    identity: Option[Identity],
    override val requestId: RequestId,
    override val headers: List[Header]
) extends Request
