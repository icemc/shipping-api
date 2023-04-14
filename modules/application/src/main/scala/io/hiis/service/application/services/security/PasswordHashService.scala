package io.hiis.service.application.services.security

import io.github.nremond.SecureHash
import zio.{ UIO, ULayer, ZIO, ZLayer }

/** Created by Abanda Ludovic on 26/10/2022 */
private[security] trait PasswordHashService {

  /**
   * Computes the hash of a password
   * @param password
   *   password to be hashed
   * @return
   *   the hashed password
   */
  def hash(password: String): UIO[String]

  /**
   * Validate a password against a hashed password
   * @param password
   *   the plain password
   * @param hashedPassword
   *   the hashed password
   * @return
   *   true if passwords are identical
   */
  def validate(password: String, hashedPassword: String): UIO[Boolean]
}

final private[security] case class SecuredPasswordHasher() extends PasswordHashService {

  /**
   * Computes the hash of a password
   *
   * @param password
   *   password to be hashed
   * @return
   *   the hashed password
   */
  override def hash(password: String): UIO[String] = ZIO.succeed(SecureHash.createHash(password))

  /**
   * Validate a password against a hashed password
   *
   * @param password
   *   the plain password
   * @param hashedPassword
   *   the hashed password
   * @return
   *   true if passwords are identical
   */
  override def validate(password: String, hashedPassword: String): UIO[Boolean] =
    ZIO.succeed(SecureHash.validatePassword(password, hashedPassword))
}

object PasswordHashService {
  val live: ULayer[SecuredPasswordHasher] = ZLayer.succeed(SecuredPasswordHasher())
}
