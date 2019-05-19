package com.sderosiaux.heatzy.webservice

import org.http4s.{EntityDecoder, Request}
import scalaz.zio.TaskR

trait WebService[-R] extends Serializable {
  def ws: WebService.Service[R]
}
object WebService {
  trait Service[R] extends Serializable {
    def fetchAs[T](req: Request[TaskR[R, ?]])(implicit d: EntityDecoder[TaskR[R, ?], T]): TaskR[R, T]
  }
}
