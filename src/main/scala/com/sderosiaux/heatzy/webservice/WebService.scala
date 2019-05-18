package com.sderosiaux.heatzy.webservice

import org.http4s.{EntityDecoder, Request}
import scalaz.zio.console.Console
import scalaz.zio.{Runtime, ZIO}

trait WebService extends Serializable {
  def webService: WebService.Service[Any]
}

object WebService extends Serializable {

  trait Service[R] {
    type F[T] = ZIO[R with Console, Throwable, T]
    def fetchAs[T](req: Request[F])(implicit d: EntityDecoder[F, T], r: Runtime[Console]): F[T]
  }

}
