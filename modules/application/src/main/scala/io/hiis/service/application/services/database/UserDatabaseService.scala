package io.hiis.service.application.services.database

import io.hiis.service.core.models.Config.MongodbConfig
import io.hiis.service.core.models.auth.User
import mongo4cats.bson.Document
import mongo4cats.zio.{ ZMongoClient, ZMongoCollection }
import zio.{ Task, ZLayer }

trait UserDatabaseService extends MongodbService[User]

final case class UserDatabaseServiceImpl(mongodbConfig: MongodbConfig, client: ZMongoClient)
    extends UserDatabaseService {
  override protected def collection: Task[ZMongoCollection[Document]] = for {
    database   <- client.getDatabase(mongodbConfig.database)
    collection <- database.getCollection("Users")
  } yield collection
}

object UserDatabaseService {
  val live = ZLayer.fromFunction(UserDatabaseServiceImpl.apply _)
}
