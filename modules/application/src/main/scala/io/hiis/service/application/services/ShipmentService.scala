package io.hiis.service.application.services

import io.circe.syntax.EncoderOps
import io.hiis.service.application.services.database.{
  ShipmentDatabaseService,
  ShipmentDatabaseServiceImpl
}
import io.hiis.service.core.models.Config.MongodbConfig
import io.hiis.service.core.models.auth.SecuredRequest
import io.hiis.service.core.models.shipping.Shipment
import mongo4cats.bson.Document
import mongo4cats.bson.syntax._
import mongo4cats.zio.ZMongoClient
import zio.{ Task, ZIO, ZLayer }

import scala.collection.Seq

trait ShipmentService {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id
   *   The ID to retrieve a shipment.
   * @return
   *   The retrieved shipment or None if no shipment could be retrieved for the given ID.
   */
  def retrieve(id: String): Task[Option[Shipment]]

  /**
   * Saves a shipment.
   *
   * @param shipment
   *   The shipment to save.
   * @return
   *   The saved shipment.
   */
  def save(shipment: Shipment)(implicit securedRequest: SecuredRequest): Task[Shipment]

  /**
   * Updates a shipment.
   *
   * @param shipment
   *   The shipment to save.
   * @return
   *   The updated shipment.
   */
  def update(shipment: Shipment)(implicit securedRequest: SecuredRequest): Task[Shipment]

  /**
   * Finds the shipment stored by a particular user
   *
   * @param securedRequest
   *   the request containing user id
   */
  def retrieveAll(implicit securedRequest: SecuredRequest): Task[Seq[Shipment]]

  /**
   * Removes the shipment for the given ID.
   *
   * @param id
   *   The ID of the shipment to be removed.
   * @return
   *   A future to wait for the process to be completed.
   */
  def delete(id: String)(implicit securedRequest: SecuredRequest): Task[Unit]
}

final case class ShipmentServiceImpl(databaseService: ShipmentDatabaseService)
    extends ShipmentService {
  override def retrieve(id: String): Task[Option[Shipment]] =
    databaseService.get(Document("shipmentID" := id))

  override def save(shipment: Shipment)(implicit securedRequest: SecuredRequest): Task[Shipment] =
    databaseService.save(shipment.copy(managerId = Some(securedRequest.identity.id)))

  override def update(shipment: Shipment)(implicit securedRequest: SecuredRequest): Task[Shipment] =
    databaseService.updateOne(
      Document("shipmentID" := shipment.shipmentID, "managerId" := securedRequest.identity.id),
      Document("$set"       := Document.parse(shipment.asJson.noSpaces))
    ) *> ZIO.succeed(shipment)

  override def retrieveAll(implicit securedRequest: SecuredRequest): Task[Seq[Shipment]] =
    databaseService.getMany(Document("managerId" := securedRequest.identity.id))

  override def delete(id: String)(implicit securedRequest: SecuredRequest): Task[Unit] =
    databaseService.deleteOne(
      Document("shipmentID" := id, "managerId" := securedRequest.identity.id)
    )
}

object ShipmentService {
  val live: ZLayer[ZMongoClient with MongodbConfig, Nothing, ShipmentService] = ZLayer.fromZIO(
    for {
      mongodbConfig <- ZIO.service[MongodbConfig]
      client        <- ZIO.service[ZMongoClient]
    } yield ShipmentServiceImpl(ShipmentDatabaseServiceImpl(mongodbConfig, client))
  )
}
