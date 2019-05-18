package com.sderosiaux.heatzy.webservice

import cats.implicits._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.{RequestLogger, ResponseLogger}
import org.http4s.{EntityDecoder, Request}
import scalaz.zio.console.{Console, putStrLn}
import scalaz.zio.interop.catz._
import scalaz.zio.{DefaultRuntime, ZIO}

import scala.concurrent.ExecutionContext.Implicits.global

trait WebServiceLoggerLive extends WebService.Service[Console] {
  implicit val rt = new DefaultRuntime {}

  override def fetchAs[T](req: Request[ZIO[Console, Throwable, ?]])(implicit d: EntityDecoder[ZIO[Console, Throwable, ?], T]): ZIO[Console, Throwable, T] = {
    BlazeClientBuilder[ZIO[Console, Throwable, ?]](global).resource.use { c =>
      withLogging(c).fetchAs[T](req)
    }
  }

  private def withLogging[T](c: Client[ZIO[Console, Throwable, ?]]): Client[ZIO[Console, Throwable, ?]] = {
    ResponseLogger(logHeaders = true, logBody = true, logAction = log.some)(
      RequestLogger(logHeaders = true, logBody = true, logAction = log.some)(
        c
      )
    )
  }

  private val log: String => ZIO[Console, Throwable, Unit] = (x: String) => putStrLn(x)

}
