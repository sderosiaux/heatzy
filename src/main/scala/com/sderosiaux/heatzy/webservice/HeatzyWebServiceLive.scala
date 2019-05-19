package com.sderosiaux.heatzy.webservice

import com.sderosiaux.heatzy.config.HeatzyConfiguration
import org.http4s.headers.{Accept, `Content-Type`}
import org.http4s.{EntityDecoder, EntityEncoder, Header, Headers, MediaType, Method, Request, Uri}
import scalaz.zio.ZIO

object HeatzyWebServiceLive extends HeatzyWebService.Service[HeatzyConfiguration with WebService[Any]] {

  override def post[A, B](path: String, body: A)(implicit enc: EntityEncoder[ZIO[Any, Throwable, ?], A], dec: EntityDecoder[ZIO[Any, Throwable, ?], B]): ZIO[HeatzyConfiguration with WebService[Any], Throwable, B] = {
    ZIO.accessM[HeatzyConfiguration with WebService[Any]](env => {
      val req = Request[ZIO[Any, Throwable, ?]](
        method = Method.POST,
        uri = Uri.unsafeFromString(s"${env.config.url}$path"),
        headers = Headers.of(
          Accept(MediaType.application.json),
          `Content-Type`(MediaType.application.json),
          Header("X-Gizwits-Application-Id", env.config.appId)
        )
      ).withEntity(body)

      env.ws.fetchAs[B](req)
    })
  }

  override def get[A](path: String, token: String)(implicit dec: EntityDecoder[ZIO[Any, Throwable, ?], A]): ZIO[HeatzyConfiguration with WebService[Any], Throwable, A] = {
    ZIO.accessM[HeatzyConfiguration with WebService[Any]](env => {
      val res = Request[ZIO[Any, Throwable, ?]](
        method = Method.GET,
        uri = Uri.unsafeFromString(s"${env.config.url}$path"),
        headers = Headers.of(
          Accept(MediaType.application.json),
          `Content-Type`(MediaType.application.json),
          Header("X-Gizwits-Application-Id", env.config.appId),
          Header("X-Gizwits-User-token", token)
        )
      )

      env.ws.fetchAs[A](res)
    })
  }

}
