package com.example.akkas.auth

import com.example.akkas.models.AuthData

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by yilong on 2018/1/8.
  */
class RedisAuthDataStorage()(implicit executionContext: ExecutionContext) extends AuthDataStorage {
  override def findAuthData(login: String): Future[Option[AuthData]] = ???

  override def saveAuthData(authData: AuthData): Future[AuthData] = ???
}
