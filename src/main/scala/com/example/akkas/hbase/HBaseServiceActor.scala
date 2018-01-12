package com.example.akkas.hbase

import akka.actor.{Actor, Props}
import com.example.akkaj.hbase.conn.{HBaseConnector, RowResource}
import com.example.akkaj.utils.Serde
import com.example.akkas.models.HBaseQueryRowSpec
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.Table
import org.apache.hadoop.hbase.rest.RowSpec

/**
  * Created by yilong on 2018/1/8.
  */

object HBaseServiceActor {
  def props(conf : Configuration, loginConfPath: String, kr5Path: String, loginKey: String): Props =
    Props(new HBaseServiceActor(conf, loginConfPath, kr5Path, loginKey))

  final case class RecordTemperature(requestId: Long, value: Double)
  final case class TemperatureRecorded(requestId: Long)

  final case class ReadTemperature(requestId: Long)
  final case class RespondTemperature(requestId: Long, value: Option[Double])
}

class HBaseServiceActor(conf : Configuration, loginConfPath: String, kr5Path: String, loginKey: String) extends Actor {
  val hBaseConnector : HBaseConnector = new HBaseConnector(conf,loginConfPath,kr5Path,loginKey)

  override def preStart(): Unit = {
    println("HBaseServiceActor first started")
    //context.actorOf(Props[HBaseServiceActor], "second")
  }
  override def postStop(): Unit = {
    hBaseConnector.shutdown()
    println("HBaseServiceActor first stopped")
  }

  override def receive: Receive = {
    case "stop" => context.stop(self)
    case HBaseQueryRowSpec(username, tablename, rowspec) =>
      var table : Table = null
      table = hBaseConnector.getTable(username, tablename)
      val model = RowResource.get(table, new RowSpec(rowspec), true)
      val msg = model match {
        case null => "hbase RowResource get model is null! "
        case _ => Serde.xmlToJson(Serde.object2Xml(model))
      }
      sender() ! msg
  }
}
