package com.example.akkas.nettys

import com.example.akkas.hbase.HBaseActorsWrap
import io.netty.channel.{ChannelInitializer, ChannelPipeline}
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpServerCodec, HttpServerExpectContinueHandler}
import io.netty.handler.ssl.SslContext

import scala.concurrent.ExecutionContext

/**
  * Created by yilong on 2018/1/11.
  */
class HttpSimpleServerInitializer(sslCtx: SslContext, hBaseActorsWrap : HBaseActorsWrap)
  extends ChannelInitializer[SocketChannel] {

  override def initChannel (ch: SocketChannel): Unit = {
    val p: ChannelPipeline = ch.pipeline
    if (sslCtx != null) {
      p.addLast (sslCtx.newHandler (ch.alloc) )
    }
    p.addLast (new HttpServerCodec)
    p.addLast (new HttpServerExpectContinueHandler)
    p.addLast (new HttpSimpleServerHandler(hBaseActorsWrap))
  }
}
