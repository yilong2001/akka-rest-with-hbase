package com.example.akka

import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.example.akka.auth.AuthService
import com.example.akka.hbase.HBaseServiceActor
import com.example.akka.utils.{Config, KbrUtil}
import org.apache.hadoop.hbase.HBaseConfiguration

import scala.concurrent.ExecutionContext
import scala.io.StdIn
import scala.util.{Failure, Success}

/**
  * Created by yilong on 2018/1/6.
  */
object Boot extends App {

  def startApplication() = {
    implicit val actorSystem = ActorSystem("mySystem")
    implicit val executor: ExecutionContext = actorSystem.dispatcher
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val config = Config.load()
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

    implicit val executionContext = actorSystem.dispatchers.lookup("my-blocking-dispatcher")
    // config of dispatcher

    val actors = for (i <- 0 to actorNum) yield actorSystem.actorOf(HBaseServiceActor.props(conf,loginConfPath,kr5Path,loginKey).withDispatcher("my-blocking-dispatcher"),
      "hbaseActor"+i)

    //val hBaseServiceActor = actorSystem.actorOf(HBaseServiceActor.props(conf,loginConfPath,kr5Path,loginKey).withDispatcher("my-blocking-dispatcher"),
    //  "hbaseActor")

    val authService = new AuthService()
    val httpRoute = new HttpRoute(authService, actors)

    val bindingFuture = Http().bindAndHandle(httpRoute.route, config.http.host, config.http.port)

    println(s"Server is online! \nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => actorSystem.terminate()) // and shutdown when done

    //bindingFuture.onComplete {
    //  case Success(res) => println(res)
    //  case Failure(_)   => sys.error("something wrong")
    //}
  }

  startApplication()
}



