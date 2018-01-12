package com.example.akkas.auth

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive0, Directive1}

/**
  * Created by yilong on 2018/1/8.
  */
trait AuthDirectives {
  val INVALID_TOKEN_MESSAGE = "The provided token is not valid."

  def authToken(authService: AuthService): Directive0 = {
    extractToken.flatMap(validateToken(authService))
  }

  def validateToken(authService: AuthService)(token: Option[String]): Directive0 = {
    token match {
      case Some(_) => validate(authService.validateToken(authService.extractToken(token)), INVALID_TOKEN_MESSAGE)
      case None => reject(AuthorizationFailedRejection)
    }
  }

  def extractToken: Directive1[Option[String]] = {
    parameter('token.?)
  }
}
