package com.example.akkas.auth

import java.security.MessageDigest
import java.util.Base64

import com.example.akkas.models.UserInfo

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by yilong on 2018/1/7.
  */

class AuthService()(implicit executionContext: ExecutionContext) {
  def login(username: String, password: String): Future[Option[String]] = {
    val digest = MessageDigest.getInstance("MD5")
    val md5 = digest.digest((username+System.currentTimeMillis()).getBytes("utf-8"))
    Future.successful(Option.apply(Base64.getEncoder.encodeToString(md5)))
  }

  def validateToken(ui : Option[UserInfo]) : Boolean = {
    ui match {
      case Some(ui) => true
      case None => false
    }
  }

  def extractToken(token : Option[String]) : Option[UserInfo] = {
    token match {
      //here, if hbase kerberos enabled, token is the proxyed uesrname
      case Some(t) => Option.apply(UserInfo(token.get))
      case None => None
    }
  }
}

