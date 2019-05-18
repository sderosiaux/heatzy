package com.sderosiaux.heatzy.webservice

import org.http4s.{EntityDecoder, EntityEncoder}
import scalaz.zio.ZIO

trait HeatzyWebService extends Serializable {
  def heatzy: HeatzyWebService.Service[Any]
}

object HeatzyWebService extends Serializable {

  trait Service[R] extends Serializable {
    def post[A, B](path: String, body: A)(implicit enc: EntityEncoder[ZIO[Any, Throwable, ?], A], dec: EntityDecoder[ZIO[Any, Throwable, ?], B]): ZIO[R, Throwable, B]

    def get[A](path: String, token: String)(implicit dec: EntityDecoder[ZIO[Any, Throwable, ?], A]): ZIO[R, Throwable, A]
  }

}
