package com.example.akkas.hbase

import akka.actor.{ActorRef, ActorSystem}
import com.example.akkas.models.HBaseTimeoutException
import com.example.akkas.models.{HBaseQueryRowSpec, HBaseTimeoutException}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Random, Success}

/**
  * Created by yilong on 2018/1/11.
  */
case class HBaseActorsWrap(actorRefs : IndexedSeq[ActorRef], size: Int, executionContext: ExecutionContext) {

  @throws(classOf[HBaseTimeoutException])
  def sendMessage(msg : HBaseQueryRowSpec, timeout : Int) : Future[String]  = {
    import akka.pattern.ask
    import scala.concurrent.duration._

    val index = new Random(System.currentTimeMillis()).nextInt(size)

    val actor = actorRefs(index)

    val result = actor.ask(msg)(timeout seconds).mapTo[String]
    //var out : String = "default null"
    //result.onComplete({
    //  case Success(value) => System.out.print("************ onComplete : "+value); out = value
    //  case Failure(ex) => throw new HBaseTimeoutException(ex.getMessage)
    //})(executionContext)


    //out = result.result(timeout seconds)

    //System.out.print("************ sendMessage result.value.get : "+result.value.get);
    //System.out.print("************ on sendMessage : "+out);

    result
  }

  def actorsNum = actorRefs.size
}
