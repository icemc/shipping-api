package io.hiis.service.core.models.shipping

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

import java.time.Instant

case class Shipment(
    // id
    shipmentID: Option[String],
    // Senders info
    sender: ContactInfo,

    // Receivers info
    receiver: ContactInfo,

    // Shipment specific info
    from: String,
    to: String,
    status: String,
    sType: String,
    weight: String,
    freightMode: String,
    vessel: String,
    departureDate: Instant,
    pickupDate: Instant,
    currentLocation: String,
    description: String,
    comment: String,
    // Managers info
    managerId: Option[String]
)

object Shipment {

  implicit val decoder: Decoder[Shipment] = deriveDecoder
  implicit val encoder: Encoder[Shipment] = deriveEncoder

  def generateRandomInvoiceNo(): Long = {
    val random           = new scala.util.Random(new java.security.SecureRandom())
    val alphabet: String = "0123456789"
    val idLength         = 6
    Stream.continually(random.nextInt(alphabet.length)).map(alphabet).take(idLength).mkString.toLong
  }
  def generateRandomShipmentID(): String = {
    val random           = new scala.util.Random(new java.security.SecureRandom())
    val alphabet: String = "0123456789"
    val idLength         = 15
    Stream.continually(random.nextInt(alphabet.length)).map(alphabet).take(idLength).mkString
  }
}
