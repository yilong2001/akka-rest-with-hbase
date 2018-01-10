package com.example.akka

import org.apache.hadoop.conf.Configuration

/**
  * Created by yilong on 2018/1/8.
  */
package object models {
  case class AuthData(username: String, password: String)

  case class UserInfo(username:String)
  case class HBaseQueryRowSpec(username: String, tablename:String, rowspec:String)

  case class HttpConfig(host: String, port: Int)
  case class DatabaseConfig(jdbcUrl: String, username: String, password: String)
  case class Config(http: HttpConfig, database: DatabaseConfig)
}
