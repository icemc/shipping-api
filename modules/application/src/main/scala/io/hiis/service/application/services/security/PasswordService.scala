package io.hiis.service.application.services.security

import io.circe.syntax.EncoderOps
import io.hiis.service.application.services.database.PasswordDatabaseService
import io.hiis.service.core.models.Config.MongodbConfig
import io.hiis.service.core.models.auth.PasswordInfo
import mongo4cats.bson.Document
import mongo4cats.bson.syntax._
import mongo4cats.zio.ZMongoClient
import zio.{ Task, ZIO, ZLayer }

import java.time.Instant

/** Created by Abanda Ludovic on 26/10/2022 */
trait PasswordService {

  /**
   * Registers a password for the user
   * @param id
   *   user id
   * @param password
   *   the users password
   * @return
   *   the passwordInfo
   */
  def create(id: String, password: String): Task[PasswordInfo]

  /**
   * Update the users password
   * @param id
   *   user id
   * @param newPassword
   *   new user password
   * @return
   *   the updated passwordInfo
   */
  def update(id: String, newPassword: String): Task[Option[PasswordInfo]]

  /**
   * Validate a users password
   * @param id
   *   user id
   * @param password
   *   the plain password
   * @return
   */
  def validate(id: String, password: String): Task[Boolean]
}

final case class PasswordServiceImpl(
    databaseService: PasswordDatabaseService,
    hashService: PasswordHashService
) extends PasswordService {

  /**
   * Registers a password for the user
   *
   * @param id
   *   user id
   * @param password
   *   the users password
   * @return
   *   the passwordInfo
   */
  override def create(id: String, password: String): Task[PasswordInfo] = for {
    hashed <- hashService.hash(password)
    passwordInfo <- databaseService.save(
      PasswordInfo(id, List(hashed), Instant.now())
    )
  } yield passwordInfo

  /**
   * Update the users password
   *
   * @param id
   *   user id
   * @param newPassword
   *   new user password
   * @return
   *   the updated passwordInfo
   */
  override def update(id: String, newPassword: String): Task[Option[PasswordInfo]] = for {
    oldInfo <- databaseService.get(Document("id" := id))
    hashed  <- hashService.hash(newPassword)
    updatedInfo <- oldInfo
      .map(info =>
        ZIO
          .foreach(info.hashes)(password => hashService.validate(newPassword, password))
          .map(_.exists(_ == true))
          .flatMap {
            case true =>
              ZIO.fail(new Throwable("Password was already used by user, try another password"))
            case _ =>
              databaseService
                .updateOne(
                  Document("id" := id),
                  Document(
                    "$set" := Document
                      .parse(info.copy(hashes = info.hashes.+:(hashed)).asJson.noSpaces)
                  )
                )
          }
      )
      .getOrElse(ZIO.fail(new Throwable("Password Info not found")))
  } yield updatedInfo

  /**
   * Validate a users password
   *
   * @param id
   *   user id
   * @param password
   *   the plain password
   * @return
   */
  override def validate(id: String, password: String): Task[Boolean] = for {
    maybeInfo <- databaseService.get(Document("id" := id))
    matches <- maybeInfo
      .map(info => hashService.validate(password, info.hashes.head))
      .getOrElse(ZIO.succeed(false))
  } yield matches
}

object PasswordService {
  val live: ZLayer[ZMongoClient with MongodbConfig, Nothing, PasswordService] =
    ZLayer.fromZIO(for {
      mongodbConfig <- ZIO.service[MongodbConfig]
      client        <- ZIO.service[ZMongoClient]
      passwordDatabase <- ZIO
        .service[PasswordDatabaseService]
        .provide(
          PasswordDatabaseService.live,
          ZLayer.succeed(client),
          ZLayer.succeed(mongodbConfig)
        )
      passwordHasher <- ZIO.service[PasswordHashService].provide(PasswordHashService.live)
      passwordService = PasswordServiceImpl(passwordDatabase, passwordHasher)
    } yield passwordService)
}
