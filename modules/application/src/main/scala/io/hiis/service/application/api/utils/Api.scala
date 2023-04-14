package io.hiis.service.application.api.utils

import io.circe.Encoder
import io.circe.generic.auto.{ exportDecoder, exportEncoder }
import io.hiis.service.application.services.security.AuthTokenService
import io.hiis.service.core.models.Constants.CustomHeaders
import io.hiis.service.core.models.auth.RequestId.{ DUMMY_REQUEST_ID, ToRequest }
import io.hiis.service.core.models.auth.{ SecuredRequest, UnsecuredRequest, UserAwareRequest }
import io.hiis.service.core.utils.Logging
import sttp.model.{ Header, StatusCode }
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._
import sttp.tapir.{ Endpoint, EndpointOutput }
import zio.ZIO

/** Created by Abanda Ludovic on 19/01/2023 */
private[api] trait Api extends ApiTypes { self: Logging =>
  import Api.ApiError
  import Api.ApiError._

  /**
   * Provides the API error to Status code mappings using oneOf. Computes all the available status
   * code mapping, by default only BadRequest and InternalServerError are defined
   * @param otherErrors
   *   the other API errors to consider
   * @return
   *   Endpoint output with errorOut already defined
   */
  final private def ExtraErrors(
      otherErrors: Class[_ <: ApiError]*
  ): EndpointOutput.OneOf[ApiError, ApiError] = {
    val all = Seq(badRequest, internalServerError) ++ otherErrors

    val variants = all.distinct.map {
      case classType if classType.isAssignableFrom(badRequest) =>
        oneOfVariant(
          statusCode(StatusCode.BadRequest).and(jsonBody[BadRequest].description("Bad request"))
        )

      case classType if classType.isAssignableFrom(internalServerError) =>
        oneOfVariant(
          statusCode(StatusCode.InternalServerError).and(
            jsonBody[InternalServerError].description("Internal server error")
          )
        )

      case classType if classType.isAssignableFrom(serviceUnavailable) =>
        oneOfVariant(
          statusCode(StatusCode.ServiceUnavailable).and(
            jsonBody[ServiceUnavailable].description("Service unavailable")
          )
        )

      case classType if classType.isAssignableFrom(notFound) =>
        oneOfVariant(
          statusCode(StatusCode.NotFound).and(jsonBody[NotFound].description("Not found"))
        )

      case classType if classType.isAssignableFrom(forbidden) =>
        oneOfVariant(
          statusCode(StatusCode.Forbidden).and(jsonBody[Forbidden].description("Forbidden"))
        )

      case classType if classType.isAssignableFrom(unauthorized) =>
        oneOfVariant(
          statusCode(StatusCode.Unauthorized).and(
            jsonBody[Unauthorized].description("Unauthorized")
          )
        )

      case classType if classType.isAssignableFrom(conflict) =>
        oneOfVariant(
          statusCode(StatusCode.Conflict).and(jsonBody[Conflict].description("Conflict"))
        )
    }

    oneOf[ApiError](variants.head, variants.tail: _*)
  }

  /**
   * Defines a secured endpoint, that verifies if user is authenticated before proceeding
   * @param otherErrors
   *   the other APIErrors returned by this endpoints
   * @return
   *   a PartialServerEndpoint
   */
  final protected def SecuredEndpoint(
      otherErrors: Class[_ <: ApiError]*
  )(implicit
      authTokenService: AuthTokenService
  ): ZPartialServerEndpoint[
    Any,
    (String, Option[String], List[Header]),
    SecuredRequest,
    Unit,
    ApiError,
    Unit,
    Any
  ] =
    endpoint
      .securityIn(header[String](CustomHeaders.AUTH_TOKEN_HEADER))
      .securityIn(header[Option[String]](CustomHeaders.REQUEST_ID_HEADER))
      .securityIn(headers)
      .errorOut(ExtraErrors(otherErrors.+:(unauthorized): _*))
      .zServerSecurityLogic(headers =>
        for {
          user <- (authTokenService
            .getBody(headers._1) <*>
            authTokenService
              .isValid(headers._1))
            .flatMap {
              case (Some(value), true) => ZIO.succeed(value)
              case _                   => ZIO.fail(Unauthorized("Could not authenticate user"))
            }
        } yield SecuredRequest(user, headers._2.getOrElse(DUMMY_REQUEST_ID).toRequestId, headers._3)
      )

  /**
   * Defines a user aware endpoint. It isn't strict like the SecuredEndpoint since authentication is
   * not obligatory
   * @param otherErrors
   *   the other APIErrors returned by this endpoint
   * @return
   *   a PartialServerEndpoint
   */
  final protected def UserAwareEndpoint(
      otherErrors: Class[_ <: ApiError]*
  )(implicit
      authTokenService: AuthTokenService
  ): ZPartialServerEndpoint[
    Any,
    (Option[String], Option[String], List[Header]),
    UserAwareRequest,
    Unit,
    ApiError,
    Unit,
    Any
  ] =
    endpoint
      .securityIn(header[Option[String]](CustomHeaders.AUTH_TOKEN_HEADER))
      .securityIn(header[Option[String]](CustomHeaders.REQUEST_ID_HEADER))
      .securityIn(headers)
      .errorOut(ExtraErrors(otherErrors: _*))
      .zServerSecurityLogic(headers =>
        for {
          user <- ZIO
            .fromOption(headers._1)
            .flatMap(authTokenService.getBody)
            .fold(identity, identity)
        } yield UserAwareRequest(
          user,
          headers._2.getOrElse(DUMMY_REQUEST_ID).toRequestId,
          headers._3
        )
      )

  /**
   * Defines an unsecured endpoint
   * @param otherErrors
   *   the other APIErrors returned by this endpoint
   * @return
   *   an Endpoint
   */
  final protected def UnsecuredEndpoint(
      otherErrors: Class[_ <: ApiError]*
  ): ZPartialServerEndpoint[
    Any,
    (Option[String], List[Header]),
    UnsecuredRequest,
    Unit,
    ApiError,
    Unit,
    Any
  ] =
    endpoint
      .securityIn(header[Option[String]](CustomHeaders.REQUEST_ID_HEADER))
      .securityIn(headers)
      .errorOut(ExtraErrors(otherErrors: _*))
      .zServerSecurityLogic(headers =>
        ZIO.succeed(
          UnsecuredRequest(headers._1.getOrElse(DUMMY_REQUEST_ID).toRequestId, headers._2)
        )
      )

  /**
   * Defines a simple endpoint with API Error enhancement
   * @param otherErrors
   *   the other APIErrors returned by this endpoint
   * @return
   *   an Endpoint
   */
  final protected def SimpleEndpoint(
      otherErrors: Class[_ <: ApiError]*
  ): Endpoint[Unit, Unit, ApiError, Unit, Any] =
    endpoint.errorOut(ExtraErrors(otherErrors: _*))
}

object Api {

  sealed trait ApiError extends Throwable with Serializable with Product

  object ApiError {

    final case class Forbidden(message: String)                             extends ApiError
    final case class BadRequest(message: String)                            extends ApiError
    final case class NotFound(message: String)                              extends ApiError
    final case class Conflict(message: String)                              extends ApiError
    final case class Unauthorized(message: String)                          extends ApiError
    final case class InternalServerError(message: String = "Server error!") extends ApiError
    final case class ServiceUnavailable(message: String = "The service is unavailable")
        extends ApiError

    implicit val apiErrorDecoder: Encoder[ApiError] =
      Encoder[String].contramap(_.productPrefix.toLowerCase)

    // Simple definitions to help obtaining the class type of subsequent API errors
    val forbidden: Class[Forbidden]                     = classOf[Forbidden]
    val badRequest: Class[BadRequest]                   = classOf[BadRequest]
    val notFound: Class[NotFound]                       = classOf[NotFound]
    val conflict: Class[Conflict]                       = classOf[Conflict]
    val unauthorized: Class[Unauthorized]               = classOf[Unauthorized]
    val internalServerError: Class[InternalServerError] = classOf[InternalServerError]
    val serviceUnavailable: Class[ServiceUnavailable]   = classOf[ServiceUnavailable]
  }
}
