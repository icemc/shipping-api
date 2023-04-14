package io.hiis.service.application.services

import io.circe.syntax.EncoderOps
import io.hiis.service.application.services.database.{
  UserDatabaseService,
  UserDatabaseServiceImpl
}
import io.hiis.service.core.models.Config.MongodbConfig
import io.hiis.service.core.models.auth.User
import mongo4cats.bson.Document
import mongo4cats.bson.syntax._
import mongo4cats.zio.ZMongoClient
import zio.{ Task, ZIO, ZLayer }

trait UserService {

  /**
   * Save a user object to data store
   * @param user
   *   user to be saved
   * @return
   *   saved user
   */
  def save(user: User): Task[User]

  /**
   * Get a user from data store using id
   * @param id
   *   users id
   * @return
   *   user object if found
   */
  def get(id: String): Task[Option[User]]

  /**
   * Get a user from data store using identifier
   * @param phone
   *   users phone number
   * @return
   *   user object if found
   */
  def getByIdentifier(phone: String): Task[Option[User]]

  /**
   * Updates user object in data store
   * @param user
   *   the user object to be updated
   * @return
   *   updated user
   */
  def update(user: User): Task[User]

  /**
   * Verifies if an identifier is already being used
   * @param phone
   *   phone number
   * @return
   *   true if phone number is already being used otherwise false
   */
  def isIdentifierUsed(phone: String): Task[Boolean]
}

final case class UserServiceImpl(database: UserDatabaseService) extends UserService {

  /**
   * Save a user object to data store
   *
   * @param user
   *   user to be saved
   * @return
   *   saved user
   */
  override def save(user: User): Task[User] = database.save(user)

  /**
   * Get a user from data store using id
   *
   * @param id
   *   users id
   * @return
   *   user object if found
   */
  override def get(id: String): Task[Option[User]] = database.get(Document("id" := id))

  /**
   * Get a user from data store using identifier
   *
   * @param phone
   *   users phone number
   * @return
   *   user object if found
   */
  override def getByIdentifier(phone: String): Task[Option[User]] =
    database.get(Document("identifier" := phone))

  /**
   * Updates user object in data store
   *
   * @param user
   *   the user object to be updated
   * @return
   *   updated user
   */
  override def update(user: User): Task[User] =
    database.updateOne(
      Document("id"   := user.id),
      Document("$set" := Document.parse(user.asJson.noSpaces))
    ) *> ZIO.succeed(user)

  /**
   * Verifies if a identifier is already being used
   *
   * @param phone
   *   phone number
   * @return
   *   true if phone number is already being used otherwise false
   */
  override def isIdentifierUsed(phone: String): Task[Boolean] =
    database.get(Document("identifier" := phone)).map(_.isDefined)
}

object UserService {
  val live: ZLayer[ZMongoClient with MongodbConfig, Nothing, UserService] = ZLayer.fromZIO(
    for {
      mongodbConfig <- ZIO.service[MongodbConfig]
      client        <- ZIO.service[ZMongoClient]
    } yield UserServiceImpl(UserDatabaseServiceImpl(mongodbConfig, client))
  )
}
