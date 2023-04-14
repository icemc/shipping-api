package io.hiis.service.application.services.database

import io.hiis.service.core.models.Config.MongodbConfig
import io.hiis.service.core.models.auth.PasswordInfo
import mongo4cats.bson.Document
import mongo4cats.zio.{ ZMongoClient, ZMongoCollection }
import zio.{ Task, ZLayer }

trait PasswordDatabaseService extends MongodbService[PasswordInfo]

final case class PasswordDatabaseServiceImpl(mongodbConfig: MongodbConfig, client: ZMongoClient)
    extends PasswordDatabaseService {
  override protected def collection: Task[ZMongoCollection[Document]] = for {
    database   <- client.getDatabase(mongodbConfig.database)
    collection <- database.getCollection("Passwords")
  } yield collection
}

object PasswordDatabaseService {
  val live = ZLayer.fromFunction(PasswordDatabaseServiceImpl.apply _)
}
