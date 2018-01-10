package com.example.akka

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, OneForOneStrategy}
import akka.dispatch.sysmsg.Supervise
import com.example.akka.hbase.HBaseServiceActor

/**
  * Created by yilong on 2018/1/6.
  */
class BootSupervisor {
  import akka.actor.Props
  import akka.pattern.Backoff
  import akka.pattern.BackoffSupervisor
  import scala.concurrent.duration.Duration

  import akka.actor.Props

  val childProps: Props = Props.create(classOf[HBaseServiceActor])

  val supervisorProps: Props = BackoffSupervisor.props(
    Backoff.onStop(childProps,
      "myEcho",
      Duration.create(3, TimeUnit.SECONDS),
      Duration.create(30, TimeUnit.SECONDS),
      0.2))
  // adds 20% "noise" to vary the intervals slightly

  ActorSystem("mySystem").actorOf(supervisorProps, "echoSupervisor")
}
