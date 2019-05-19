package com.sderosiaux.heatzy.webservice

import com.sderosiaux.heatzy.config.HeatzyConfiguration
import com.sderosiaux.heatzy.model.{BindingsResponse, LoginRequest, LoginResponse}
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.headers.{Accept, `Content-Type`}
import org.http4s.{EntityDecoder, EntityEncoder, Header, Headers, MediaType, Method, Request, Uri}
import scalaz.zio.Task
import scalaz.zio.interop.catz._

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.zio._


class Http4sHeatzyWebService(config: HeatzyConfiguration) extends HeatzyWebService.Service[Any] {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[Task, A] = jsonOf[Task, A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[Task, A] = jsonEncoderOf[Task, A]

  implicit val rt = new DefaultRuntime {}

  override def bindings(token: String): Task[BindingsResponse] = {
    get[BindingsResponse]("/bindings", token).provide(config)
  }

  override def login(username: String, password: String): Task[LoginResponse] = {
    post[LoginRequest, LoginResponse]("/login", LoginRequest(username, password)).provide(config)
  }

  private def post[A, B](path: String, body: A)(implicit enc: EntityEncoder[Task, A], dec: EntityDecoder[Task, B]): Task[B] = {
    val req = Request[Task](
      method = Method.POST,
      uri = Uri.unsafeFromString(s"${config.config.url}$path"),
      headers = Headers.of(
        Accept(MediaType.application.json),
        `Content-Type`(MediaType.application.json),
        Header("X-Gizwits-Application-Id", config.config.appId)
      )
    ).withEntity(body)

    BlazeClientBuilder[Task](global).resource.use { c: Client[Task] =>
      //withLogging(c).fetchAs[B](req)
      c.fetchAs[B](req)
    }
  }

  private def get[A](path: String, token: String)(implicit dec: EntityDecoder[Task, A]): Task[A] = {
    val req = Request[Task](
      method = Method.GET,
      uri = Uri.unsafeFromString(s"${config.config.url}$path"),
      headers = Headers.of(
        Accept(MediaType.application.json),
        `Content-Type`(MediaType.application.json),
        Header("X-Gizwits-Application-Id", config.config.appId),
        Header("X-Gizwits-User-token", token)
      )
    )

    BlazeClientBuilder[Task](global).resource.use { c =>
      //withLogging(c).fetchAs[B](req)
      c.fetchAs[A](req)
    }
  }

  //
  //  private def withLogging[R, T](c: Client[TaskR[R with Console, ?]]): Client[TaskR[R with Console, ?]] = {
  //    ResponseLogger(logHeaders = true, logBody = true, logAction = log.some)(
  //      RequestLogger(logHeaders = true, logBody = true, logAction = log.some)(
  //        c
  //      )
  //    )
  //  }
  //
  //  private val log: String => TaskR[Console, Unit] = (x: String) => putStrLn(x)

}
