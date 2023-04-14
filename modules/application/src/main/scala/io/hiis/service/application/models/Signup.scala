package io.hiis.service.application.models

case class Signup(
    identifier: String,
    password: String,
    email: String,
    firstName: String,
    lastName: String
)
