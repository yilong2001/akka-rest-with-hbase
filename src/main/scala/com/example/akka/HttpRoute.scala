package com.example.akka

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive0, Directive1, Route}
import com.example.akka.auth.{AuthDirectives, AuthService}
import com.example.akka.routes.{AuthRoute, HBaseRestRoute}

import scala.concurrent.ExecutionContext

/**
  * Created by yilong on 2018/1/7.
  */
class HttpRoute(authService : AuthService,
                hbaseActors : IndexedSeq[ActorRef])(implicit executionContext: ExecutionContext) extends AuthDirectives {
  private val _authRouter = new AuthRoute(authService)
  private val _hbaseRoute = new HBaseRestRoute(authService, hbaseActors)

  val route: Route =
      pathPrefix("user") {
        _authRouter.route
      } ~
      pathPrefix("api" / "v1" / "hbase") {
        authToken(authService) {
          _hbaseRoute.route
        }
      }
}
