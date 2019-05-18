package com.sderosiaux.heatzy.webservice

import com.sderosiaux.heatzy.config.Heatzy
import org.http4s.headers.{Accept, `Content-Type`}
import org.http4s.{EntityDecoder, EntityEncoder, Header, Headers, MediaType, Method, Request, Uri}
import scalaz.zio.ZIO

trait HeatzyWebServiceLive extends HeatzyWebService.Service[Heatzy with WebService] {

  override def post[A, B](path: String, body: A)(implicit enc: EntityEncoder[ZIO[Any, Throwable, ?], A], dec: EntityDecoder[ZIO[Any, Throwable, ?], B]): ZIO[Heatzy with WebService, Throwable, B] = {
    ZIO.accessM(x => {
      val req = Request[ZIO[Any, Throwable, ?]](
        method = Method.POST,
        uri = Uri.unsafeFromString(s"${x.url}$path"),
        headers = Headers.of(
          Accept(MediaType.application.json),
          `Content-Type`(MediaType.application.json),
          Header("X-Gizwits-Application-Id", x.appId)
        )
      ).withEntity(body)

      x.webService.fetchAs[B](req)
    })
  }

  override def get[A](path: String, token: String)(implicit dec: EntityDecoder[ZIO[Any, Throwable, ?], A]): ZIO[Heatzy with WebService, Throwable, A] = {
    ZIO.accessM(x => {
      val res = Request[ZIO[Any, Throwable, ?]](
        method = Method.GET,
        uri = Uri.unsafeFromString(s"${x.url}$path"),
        headers = Headers.of(
          Accept(MediaType.application.json),
          `Content-Type`(MediaType.application.json),
          Header("X-Gizwits-Application-Id", x.appId),
          Header("X-Gizwits-User-token", token)
        )
      )

      x.webService.fetchAs[A](res)
    })
  }

}

object HeatzyWebServiceLive extends HeatzyWebServiceLive
