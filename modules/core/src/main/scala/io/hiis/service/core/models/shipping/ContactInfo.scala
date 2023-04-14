package io.hiis.service.core.models.shipping

import io.circe.generic.JsonCodec

@JsonCodec
case class ContactInfo(name: String, phone: String, state: String, address: String, country: String)
