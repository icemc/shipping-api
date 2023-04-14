package io.hiis.service.application.api.controllers

import io.circe.Json
import io.circe.syntax.EncoderOps
import io.hiis.service.application.api.utils.Api.ApiError.{ notFound, NotFound }
import io.hiis.service.application.api.utils.Controller
import io.hiis.service.application.services.ShipmentService
import io.hiis.service.application.services.security.AuthTokenService
import io.hiis.service.core.models.auth.{ SecuredRequest, UnsecuredRequest }
import io.hiis.service.core.models.shipping.Shipment
import io.hiis.service.core.utils.Logging
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{ path, stringJsonBody, EndpointInput }
import zio.ZIO

case class ShipmentController(shipmentService: ShipmentService)(implicit
    authTokenService: AuthTokenService
) extends Controller
    with Logging {
  override protected def BaseUrl: EndpointInput[Unit] = super.BaseUrl / "shipment"

  val getShipment = SecuredEndpoint(notFound).get
    .in(BaseUrl / "get" / path[String]("id"))
    .out(stringJsonBody)
    .serverLogic { implicit request: SecuredRequest => id: String =>
      for {
        shipment <- shipmentService.retrieve(id).flatMap {
          case Some(value) => ZIO.succeed(value)
          case None        => ZIO.fail(NotFound("Shipment not found"))
        }
      } yield shipment.asJson.noSpaces
    }

  val getShipmentPublic = UnsecuredEndpoint(notFound).get
    .in(BaseUrl / "public" / "get" / path[String]("id"))
    .out(stringJsonBody)
    .serverLogic { implicit request: UnsecuredRequest => id: String =>
      for {
        shipment <- shipmentService.retrieve(id).flatMap {
          case Some(value) => ZIO.succeed(value)
          case None        => ZIO.fail(NotFound("Shipment not found"))
        }
      } yield shipment.asJson.noSpaces
    }

  val getAll = SecuredEndpoint(notFound).get
    .in(BaseUrl / "all")
    .out(stringJsonBody)
    .serverLogic { implicit request: SecuredRequest => _: Unit =>
      for {
        shipments <- shipmentService.retrieveAll
      } yield shipments.asJson.noSpaces
    }

  val saveShipment = SecuredEndpoint(notFound).post
    .in(BaseUrl / "save")
    .in(jsonBody[Shipment])
    .out(jsonBody[Shipment])
    .serverLogic { implicit request: SecuredRequest => shipment: Shipment =>
      for {
        shipment <- shipmentService.save(
          shipment.copy(shipmentID = Some(Shipment.generateRandomShipmentID()))
        )
      } yield shipment
    }

  val updateShipment = SecuredEndpoint(notFound).post
    .in(BaseUrl / "update")
    .in(jsonBody[Shipment])
    .out(jsonBody[Shipment])
    .serverLogic { implicit request: SecuredRequest => shipment: Shipment =>
      for {
        shipment <- shipmentService.update(shipment)
      } yield shipment
    }

  val deleteShipment = SecuredEndpoint().delete
    .in(BaseUrl / "delete" / path[String]("id"))
    .out(stringJsonBody)
    .serverLogic { implicit request: SecuredRequest => id: String =>
      for {
        _ <- shipmentService.delete(id)
      } yield Json.obj("message" -> Json.fromString("Shipment deleted")).noSpaces
    }

  override def endpoints: List[_root_.sttp.tapir.ztapir.ZServerEndpoint[Any, Any]] =
    List(getShipment, getShipmentPublic, getAll, saveShipment, updateShipment, deleteShipment)
}
