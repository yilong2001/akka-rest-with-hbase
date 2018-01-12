package com.example.akkas.auth

import com.example.akkas.models.AuthData

import scala.concurrent.Future

/**
  * Created by yilong on 2018/1/7.
  */

trait AuthDataStorage {
  def findAuthData(login: String): Future[Option[AuthData]]
  def saveAuthData(authData: AuthData): Future[AuthData]
}


