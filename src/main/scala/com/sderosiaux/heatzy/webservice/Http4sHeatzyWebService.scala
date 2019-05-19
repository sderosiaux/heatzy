package com.sderosiaux.heatzy.webservice

import com.sderosiaux.heatzy.config.HeatzyConfiguration
import com.sderosiaux.heatzy.model.{BindingsResponse, LoginRequest, LoginResponse}
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.{RequestLogger, ResponseLogger}
import org.http4s.headers.{Accept, `Content-Type`}
import org.http4s.{EntityDecoder, EntityEncoder, Header, Headers, MediaType, Method, Request, Uri}
import scalaz.zio.Task
import scalaz.zio.interop.catz._
import cats.implicits._
import scalaz.zio.console._

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.zio._
import scalaz.zio.console.Console


class Http4sHeatzyWebService[R <: Console](runtime: Runtime[R], config: HeatzyConfiguration) extends HeatzyWebService.Service[Any] {

  implicit val rt = runtime
  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[TaskR[Console, ?], A] = jsonOf[TaskR[Console, ?], A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[TaskR[Console, ?], A] = jsonEncoderOf[TaskR[Console, ?], A]

  override def bindings(token: String): Task[BindingsResponse] = {
    get[BindingsResponse]("/bindings", token).provide(config)
  }

  override def login(username: String, password: String): Task[LoginResponse] = {
    post[LoginRequest, LoginResponse]("/login", LoginRequest(username, password)).provide(config)
  }

  private def post[A, B](path: String, body: A)(implicit enc: EntityEncoder[TaskR[Console, ?], A], dec: EntityDecoder[TaskR[Console, ?], B]): Task[B] = {
    val req = Request[TaskR[Console, ?]](
      method = Method.POST,
      uri = Uri.unsafeFromString(s"${config.config.url}$path"),
      headers = Headers.of(
        Accept(MediaType.application.json),
        `Content-Type`(MediaType.application.json),
        Header("X-Gizwits-Application-Id", config.config.appId)
      )
    ).withEntity(body)

    BlazeClientBuilder[TaskR[Console, ?]](global).resource.use { c: Client[TaskR[Console, ?]] =>
      withLogging(c).fetchAs[B](req)
    }.provide(rt.Environment)
  }

  private def get[A](path: String, token: String)(implicit dec: EntityDecoder[TaskR[Console, ?], A]): Task[A] = {
    val req = Request[TaskR[Console, ?]](
      method = Method.GET,
      uri = Uri.unsafeFromString(s"${config.config.url}$path"),
      headers = Headers.of(
        Accept(MediaType.application.json),
        `Content-Type`(MediaType.application.json),
        Header("X-Gizwits-Application-Id", config.config.appId),
        Header("X-Gizwits-User-token", token)
      )
    )

    BlazeClientBuilder[TaskR[Console, ?]](global).resource.use { c =>
      withLogging(c).fetchAs[A](req)
    }.provide(rt.Environment)
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
