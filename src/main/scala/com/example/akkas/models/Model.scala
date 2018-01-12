package com.example.akkas.models

/**
  * Created by yilong on 2018/1/11.
  */
case class AuthData(username: String, password: String)
case class UserInfo(username:String)

case class HBaseQueryRowSpec(username: String, tablename:String, rowspec:String)

case class HttpConfig(host: String, port: Int)
case class DatabaseConfig(jdbcUrl: String, username: String, password: String)
case class Config(http: HttpConfig, database: DatabaseConfig, netty: NettyConfig)

case class NettyConfig(ssl: Boolean, bossGroupThreads: Int, workGroupThreads: Int)

case class HBaseTimeoutException(message: String) extends Exception

