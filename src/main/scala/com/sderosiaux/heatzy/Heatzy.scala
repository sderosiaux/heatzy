package com.sderosiaux.heatzy

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.{RequestLogger, ResponseLogger}
import org.http4s.headers.{Accept, `Content-Type`}

import scala.concurrent.ExecutionContext.Implicits.global

case class LoginRequest(username: String, password: String, lang: String = "en")

case class LoginResponse(token: String, uid: String, expire_at: Long)

case class Device(protoc: Int, is_disabled: Boolean, dev_alias: String)

case class BindingsResponse(devices: List[Device])

case class Heatzy(url: String, appId: String)

/*
TODO:
- sbt hardcore
- ZIO (env)
- Database
- Refresh token consideration
- The rest
- API ?
- metrics
- config using ciris
 */

/*
https://drive.google.com/drive/folders/0B9nVzuTl4YMOaXAzRnRhdXVma1k
https://github.com/l3flo/jeedom-heatzy/blob/master/core/class/heatzy.class.php
 */

object Heatzy extends IOApp {
  val config = ConfigFactory.load()
  val heatzy = Heatzy(config.getString("heatzy.cloud.url"), config.getString("heatzy.app.id"))


  //implicit val lrDecoder: EntityDecoder[IO, LoginResponse] = jsonOf[IO, LoginResponse]

  val p: Option[String => IO[Unit]] = ((x: String) => IO(println(x))).some

  def post[A, B](path: String, body: A)(implicit d: EntityDecoder[IO, B], d2: EntityEncoder[IO, A], client: Client[IO]): IO[B] = {
    val res = Request[IO](
      method = Method.POST,
      uri = Uri.unsafeFromString(s"${heatzy.url}$path"),
      headers = Headers.of(
        Accept(MediaType.application.json),
        `Content-Type`(MediaType.application.json),
        Header("X-Gizwits-Application-Id", heatzy.appId)
      )
    ).withEntity(body)

    client.fetchAs[B](res)
  }

  def get[A](path: String, token: String)(implicit client: Client[IO], d: EntityDecoder[IO, A]): IO[A] = {
    val res = Request[IO](
      method = Method.GET,
      uri = Uri.unsafeFromString(s"${heatzy.url}$path"),
      headers = Headers.of(
        Accept(MediaType.application.json),
        `Content-Type`(MediaType.application.json),
        Header("X-Gizwits-Application-Id", heatzy.appId),
        Header("X-Gizwits-User-token", token)
      )
    )

    client.fetchAs[A](res)
  }

  def bindings(token: String)(implicit client: Client[IO]): IO[BindingsResponse] = {
    get[BindingsResponse]("/bindings", token)
  }

  def login(username: String, password: String)(implicit client: Client[IO]): IO[LoginResponse] = {
    val body = LoginRequest(username, password)
    post[LoginRequest, LoginResponse]("/login", body)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeClientBuilder[IO](global).resource.use { c =>
      implicit val client: Client[IO] = ResponseLogger(logHeaders = true, logBody = true, logAction = p)(
        RequestLogger(logHeaders = true, logBody = true, logAction = p)(c)
      )

      for {
        l <- login("XXX@gmail.com", "XXX")
        b <- bindings(l.token)
        _ <- IO(println(b))
      } yield ExitCode.Success
    }
  }
}
