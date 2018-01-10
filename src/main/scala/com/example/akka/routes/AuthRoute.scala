package com.example.akka.routes

import scala.concurrent.ExecutionContext
import akka.http.scaladsl.model.StatusCodes

import akka.http.scaladsl.server.Directives._

import com.example.akka.auth.AuthService

/**
  * Created by yilong on 2018/1/7.
  */

class AuthRoute(authService : AuthService)(implicit executionContext: ExecutionContext) {
  var route =
  path("login") {
    parameters('username, 'password) {
      (username, password) => {
        get {
          complete(
            authService.login(username, password).map {
              case Some(token) => StatusCodes.OK -> token
              case None => StatusCodes.Unauthorized -> "wrong login"
            }
          )
        }
      }
    }
  }
}
