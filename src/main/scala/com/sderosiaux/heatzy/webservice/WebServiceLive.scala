package com.sderosiaux.heatzy.webservice

import cats.implicits._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.{RequestLogger, ResponseLogger}
import org.http4s.{EntityDecoder, Request}
import scalaz.zio.console.{Console, putStrLn}
import scalaz.zio.interop.catz._
import scalaz.zio.{Runtime, ZIO}

import scala.concurrent.ExecutionContext.Implicits.global

trait WebServiceLive extends WebService {
  override def webService: WebService.Service[Any] = new WebService.Service[Any] {

    override def fetchAs[T](req: Request[F])(implicit d: EntityDecoder[F, T], r: Runtime[Console]): F[T] = {
      BlazeClientBuilder[F](global).resource.use { c =>
        withLogging(c).fetchAs[T](req)
      }
    }

    private def withLogging[T](c: Client[F]): Client[F] = {
      ResponseLogger(logHeaders = true, logBody = true, logAction = log.some)(
        RequestLogger(logHeaders = true, logBody = true, logAction = log.some)(
          c
        )
      )
    }

    private val log: String => ZIO[Console, Throwable, Unit] = (x: String) => putStrLn(x)
  }

}
