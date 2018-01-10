package com.example.akka.utils

/**
  * Created by yilong on 2018/1/7.
  */
import com.example.akka.models.Config
import pureconfig.loadConfig

object Config {
  def load() =
    loadConfig[Config] match {
      case Right(config) => config
      case Left(error) =>
        throw new RuntimeException("Cannot read config file, errors:\n" + error.toList.mkString("\n"))
    }
}
