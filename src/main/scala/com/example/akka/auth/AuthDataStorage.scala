package com.example.akka.auth

import com.example.akka.models.AuthData

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by yilong on 2018/1/7.
  */

trait AuthDataStorage {
  def findAuthData(login: String): Future[Option[AuthData]]
  def saveAuthData(authData: AuthData): Future[AuthData]
}


