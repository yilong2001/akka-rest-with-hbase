package com.example.akkas.nettys


import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.example.akkas.hbase.HBaseActorBuilder
import com.example.akkas.utils.ConfigS
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import org.apache.hadoop.hbase.HBaseConfiguration

import scala.concurrent.ExecutionContext

/**
  * Created by yilong on 2018/1/11.
  */
object HttpSimpleServer {
  def main(args: Array[String]) {
    implicit val actorSystem = ActorSystem("mySystem")
    implicit val executor: ExecutionContext = actorSystem.dispatchers.lookup("my-blocking-dispatcher")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val config = ConfigS.load()
    val conf = HBaseConfiguration.create()

    var loginConfPath : String = null
    var kr5Path : String  = null
    var loginKey : String  = null
    var actorNum = 5
    if (args.length > 2) {
      loginConfPath = args(0)
      kr5Path = args(1)
      loginKey = args(2)
      actorNum = Integer.parseInt(args(3))
      println(args(0) + " : " + args(1) + " : " + args(2))
    }

    val bossGroup = new NioEventLoopGroup(config.netty.bossGroupThreads)

    val workerGroup = new NioEventLoopGroup
    try {
      val b = new ServerBootstrap
      b.option[Integer](ChannelOption.SO_BACKLOG, 1024)

      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new HttpSimpleServerInitializer(null,
          HBaseActorBuilder.createActors(config, args, executor)))

      val ch = b.bind(config.http.host, config.http.port).sync.channel

      println("Open your web browser and navigate to " + config.http.host + ":" + config.http.port + '/')
      ch.closeFuture.sync
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }
}
