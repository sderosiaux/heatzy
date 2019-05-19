package com.sderosiaux.heatzy.webservice

import cats.implicits._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.{RequestLogger, ResponseLogger}
import org.http4s.{EntityDecoder, Request}
import scalaz.zio.console.{Console, putStrLn}
import scalaz.zio.interop.catz._
import scalaz.zio.{DefaultRuntime, Task, TaskR, ZIO}

import scala.concurrent.ExecutionContext.Implicits.global

object WebServiceLoggerLive extends WebService.Service[Console] {
  implicit val rt = new DefaultRuntime {}

  // Throwable as error channel because we need to summon a cats's Concurrent

  override def fetchAs[T](req: Request[TaskR[Console, ?]])(implicit d: EntityDecoder[TaskR[Console, ?], T]): ZIO[Console, Throwable, T] = {
    BlazeClientBuilder[TaskR[Console, ?]](global).resource.use { c =>
      withLogging(c).fetchAs[T](req)
    }
  }

  private def withLogging[T](c: Client[TaskR[Console, ?]]): Client[TaskR[Console, ?]] = {
    ResponseLogger(logHeaders = true, logBody = true, logAction = log.some)(
      RequestLogger(logHeaders = true, logBody = true, logAction = log.some)(
        c
      )
    )
  }

  private val log: String => TaskR[Console, Unit] = (x: String) => putStrLn(x)

}
