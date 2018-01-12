package com.example.akkas.nettys

import com.example.akkas.hbase.HBaseActorsWrap
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http.{FullHttpResponse, HttpRequest, HttpUtil}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by yilong on 2018/1/11.
  */
class HttpSimpleServerHandler(hBaseActorsWrap : HBaseActorsWrap) extends ChannelInboundHandlerAdapter {
  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    if (msg.isInstanceOf[HttpRequest]) {
      val req = msg.asInstanceOf[HttpRequest]
      val keepAlive = HttpUtil.isKeepAlive(req)
      val rsp = HBaseHandler.process(req, hBaseActorsWrap)

      if (!keepAlive) ctx.write(rsp).addListener(ChannelFutureListener.CLOSE)
      else ctx.write(rsp)

//      response.onComplete({
//        case Success(rsp) => {
//          if (!keepAlive) ctx.write(rsp).addListener(ChannelFutureListener.CLOSE)
//          else ctx.write(rsp)
//        }
//        case Failure(e) => {
//          val r = HBaseHandler.buildHttp5xxResponse(req, e.getMessage)
//          if (!keepAlive) ctx.write(r).addListener(ChannelFutureListener.CLOSE)
//          else ctx.write(r)
//        }
//      })
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close
  }
}