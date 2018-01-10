package com.example.akka.routes

import akka.event.Logging
import akka.actor.{ActorRef, ActorSystem}

import scala.concurrent.ExecutionContext
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.example.akka.auth.AuthService
import com.example.akka.models.{HBaseQueryRowSpec, UserInfo}
import org.apache.commons.logging.{Log, LogFactory}

import scala.util.{Failure, Random, Success}


/**
  * Created by yilong on 2018/1/7.
  */
class HBaseRestRoute(authService : AuthService, hbaseActors : IndexedSeq[ActorRef])(implicit executionContext: ExecutionContext) {
  //val actorSystem = ActorSystem("mySystem")
  //val log = Logging(actorSystem, hbaseActor)
  private val LOG = LogFactory.getLog(classOf[HBaseRestRoute])
  private val actorNum = hbaseActors.size
  private val rand = new Random()

  val tokenGetWithURI =
    parameter('token.?) & (get) & extractUnmatchedPath

  val route =
    pathPrefix("get") {
      tokenGetWithURI  {
        (token, url) => {
          LOG.info("HBaseRestRoute visit : " + url.toString())
          val userInfo = authService.extractToken(token).get
          val urls = url.toString().split("/")
          if (urls.length < 2) {
            throw new IllegalArgumentException("url error : " + url.toString())
          }
          val table = urls(1)
          val rowspec = url.toString().substring(urls(1).length+1)
          //System.out.println(table, rowspec)

          import akka.pattern.ask
          import scala.concurrent.duration._
          val curId = rand.nextInt(actorNum)
          val result = hbaseActors(curId).ask(HBaseQueryRowSpec(userInfo.username, table, rowspec))(5 seconds)
          onComplete(result.mapTo[String]) {
            case Success(value) => complete(StatusCodes.OK -> value)
            case Failure(ex)    => complete((StatusCodes.InternalServerError, s"hbase service error : ${ex.getMessage}"))
          }
        }
      }
    }

}
