package io.hiis.service.application.services.database

import io.hiis.service.core.models.Config.MongodbConfig
import io.hiis.service.core.models.shipping.Shipment
import mongo4cats.bson.Document
import mongo4cats.zio.{ ZMongoClient, ZMongoCollection }
import zio.{ Task, ZLayer }

trait ShipmentDatabaseService extends MongodbService[Shipment]

final case class ShipmentDatabaseServiceImpl(mongodbConfig: MongodbConfig, client: ZMongoClient)
    extends ShipmentDatabaseService {
  override protected def collection: Task[ZMongoCollection[Document]] = for {
    database   <- client.getDatabase(mongodbConfig.database)
    collection <- database.getCollection("Shipments")
  } yield collection
}

object ShipmentDatabaseService {
  val live = ZLayer.fromFunction(ShipmentDatabaseServiceImpl.apply _)
}
