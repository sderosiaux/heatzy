package com.sderosiaux.heatzy.webservice

import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.{RequestLogger, ResponseLogger}
import org.http4s.{EntityDecoder, Request}
import scalaz.zio.console.{Console, putStrLn}
import scalaz.zio.{Runtime, TaskR, ZIO}
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.zio.interop.catz._

trait WebServiceLive extends WebService  {
  override def webService: WebService.Service = new WebService.Service {
    override def fetchAs[T](req: Request[TaskR[Console, ?]])(implicit d: EntityDecoder[TaskR[Console, ?], T], r: Runtime[Console]): TaskR[Console, T] = {
      BlazeClientBuilder[TaskR[Console, ?]](global).resource.use { c =>
        withLogging(c).fetchAs[T](req)
      }
    }
  }

  private val log: String => ZIO[Console, Throwable, Unit] = (x: String) => putStrLn(x)

  private def withLogging[T](c: Client[TaskR[Console, ?]]): Client[TaskR[Console, ?]] = {
    ResponseLogger(logHeaders = true, logBody = true, logAction = log.some)(
      RequestLogger(logHeaders = true, logBody = true, logAction = log.some)(
        c
      )
    )
  }
}
