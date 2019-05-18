package com.sderosiaux.heatzy.webservice

import org.http4s.{EntityDecoder, Request}
import scalaz.zio.ZIO

trait WebService extends Serializable {
  def webService: WebService.Service[Any]
}

object WebService extends Serializable {

  trait Service[R] extends Serializable {
    def fetchAs[T](req: Request[ZIO[R, Throwable, ?]])(implicit d: EntityDecoder[ZIO[R, Throwable, ?], T]): ZIO[R, Throwable, T]
  }

}
