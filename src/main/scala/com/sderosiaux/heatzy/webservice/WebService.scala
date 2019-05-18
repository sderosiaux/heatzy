package com.sderosiaux.heatzy.webservice

import org.http4s.{EntityDecoder, Request}
import scalaz.zio.console.Console
import scalaz.zio.{Runtime, TaskR}

trait WebService extends Serializable {
  def webService: WebService.Service
}

object WebService {

  trait Service {
    def fetchAs[T](req: Request[TaskR[Console, ?]])(implicit d: EntityDecoder[TaskR[Console, ?], T], r: Runtime[Console]): TaskR[Console, T]
  }

}
