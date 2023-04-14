package io.hiis.service.core.models

object Constants {
  object CustomHeaders {
    val REQUEST_ID_HEADER    = "X-REQUEST-ID"
    val AUTH_TOKEN_HEADER    = "X-AUTH-TOKEN"
    val REFRESH_TOKEN_HEADER = "X-REFRESH-TOKEN"

    val ALL_CUSTOM_HEADERS = List(REQUEST_ID_HEADER, AUTH_TOKEN_HEADER, REFRESH_TOKEN_HEADER)

    val ALL_IMPORTANT_HEADERS = ALL_CUSTOM_HEADERS ++ List("Authorization")
  }
}
