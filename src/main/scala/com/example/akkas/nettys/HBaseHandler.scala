package com.example.akkas.nettys

import java.util
import java.util.{List, Map}

import com.example.akkas.hbase.HBaseActorsWrap
import com.example.akkas.models.{HBaseQueryRowSpec, HBaseTimeoutException}
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.HttpResponseStatus.OK
import io.netty.handler.codec.http.HttpVersion.HTTP_1_1
import io.netty.handler.codec.http._
import io.netty.util.AsciiString
import org.apache.commons.io.Charsets
import org.apache.commons.logging.LogFactory
import org.json.JSONObject

import scala.concurrent._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by yilong on 2018/1/11.
  */
object HBaseHandler {
  private val LOG = LogFactory.getLog(classOf[HttpSimpleServerHandler])

  val TOKEN = "token"
  val BASE_URI = "/api/v1/hbase/get/"

  val CONTENT_TYPE = AsciiString.of("Content-Type")
  val CONTENT_LENGTH = AsciiString.of("Content-Length")
  val CONNECTION = AsciiString.of("Connection")
  val KEEP_ALIVE = AsciiString.of("keep-alive")

  private def buildHttpResponse(status:HttpResponseStatus, info:String, keepAlive:Boolean) = {
    val response = new DefaultFullHttpResponse(HTTP_1_1,
      status,
      Unpooled.wrappedBuffer(info.getBytes(Charsets.UTF_8)))

    response.headers.set(CONTENT_TYPE, "application/json")
    response.headers.set(CONTENT_LENGTH, response.content.readableBytes)

    if (keepAlive) {
      response.headers.set(CONNECTION, KEEP_ALIVE)
    }

    response
  }

  def buildHttp401Response(req: HttpRequest): FullHttpResponse = {
    val keepAlive = HttpUtil.isKeepAlive(req)
    buildHttpResponse(HttpResponseStatus.UNAUTHORIZED, "{result:\"unAuth\"}", keepAlive)
  }

  def buildHttp5xxResponse(req: HttpRequest, error: String): FullHttpResponse = {
    val keepAlive = HttpUtil.isKeepAlive(req)
    buildHttpResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, ("{result:" + error + "}"), keepAlive)
  }

  private def buildHttpResponse(req: HttpRequest, rsp: Any): FullHttpResponse = {
    val keepAlive: Boolean = HttpUtil.isKeepAlive(req)

    val out = (rsp.isInstanceOf[String]) match {
      case true => rsp.asInstanceOf[String]
      case _ => new JSONObject(rsp).toString
    }

    buildHttpResponse(HttpResponseStatus.OK, out, keepAlive)
  }

  @throws(classOf[HBaseTimeoutException])
  private def performQuery(token:String, uri:String, req: HttpRequest, hBaseActorsWrap : HBaseActorsWrap) :
    String = {
    val hbaseuri: String = uri.substring(BASE_URI.length)
    val tmps: Array[String] = hbaseuri.split("/")
    val rowspec : String = hbaseuri.substring(tmps(0).length+1)
    val start = System.currentTimeMillis()
    LOG.info("************ performQuery: token:"+token+";  hbase uri : "+hbaseuri+";  table:"+tmps(0)+";  rowspec:"+rowspec)

    val result = hBaseActorsWrap.sendMessage(HBaseQueryRowSpec(token, tmps(0), rowspec), 5)
    val r = Await.result(result, 5 seconds)

    LOG.info("************ performQuery: result:"+r+"; time:"+(System.currentTimeMillis() - start))
    r
  }

  def process(req: HttpRequest, hBaseActorsWrap : HBaseActorsWrap): FullHttpResponse = {
    val url = req.getUri
    val qdecoder = new QueryStringDecoder(url)
    val httpReqParams = qdecoder.parameters
    val uri = qdecoder.path
    val token = (httpReqParams.containsKey(TOKEN)) match {
      case true => httpReqParams.get(TOKEN).get(0)
      case _ => null
    }

    token match {
      case null => (buildHttp401Response(req))
      case _ => {
        try {
           val r = performQuery(token, uri, req, hBaseActorsWrap)
          buildHttpResponse(req, r)
        } catch {
          case ex: HBaseTimeoutException => buildHttp5xxResponse(req, ex.getMessage())
          case e : Throwable => buildHttp5xxResponse(req, "error : "+e.getMessage)
        }
      }
    }
  }
}
