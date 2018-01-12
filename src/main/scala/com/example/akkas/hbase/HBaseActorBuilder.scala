package com.example.akkas.hbase

import akka.actor.ActorSystem
import com.example.akkas.models.Config
import org.apache.hadoop.hbase.HBaseConfiguration

import scala.concurrent.ExecutionContext

/**
  * Created by yilong on 2018/1/11.
  */
object HBaseActorBuilder {
  def createActors(config : Config, args : Array[String], executionContext: ExecutionContext) : HBaseActorsWrap = {
    implicit val actorSystem = ActorSystem("mySystem")

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

    val actors = for (i <- 0 to actorNum) yield actorSystem.actorOf(HBaseServiceActor.props(conf,loginConfPath,kr5Path,loginKey).withDispatcher("my-blocking-dispatcher"),
      "hbaseActor"+i)

    HBaseActorsWrap(actors, actors.size, executionContext)
  }

}
