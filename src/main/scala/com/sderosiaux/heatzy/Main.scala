package com.sderosiaux.heatzy

import com.sderosiaux.heatzy.config.Heatzy
import com.sderosiaux.heatzy.model.{BindingsResponse, LoginRequest, LoginResponse}
import com.sderosiaux.heatzy.webservice.{WebService, WebServiceLive}
import com.typesafe.config.ConfigFactory
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.headers.{Accept, `Content-Type`}
import org.http4s.{EntityDecoder, EntityEncoder, Header, Headers, MediaType, Method, Request, Uri}
import scalaz.zio.console.{Console, putStrLn}
import scalaz.zio.{TaskR, ZIO}
import com.sderosiaux.heatzy.model.{BindingsResponse, LoginRequest, LoginResponse}
import com.sderosiaux.heatzy.webservice.{WebService, WebServiceLive}
import scalaz.zio._
import scalaz.zio.console._
import scalaz.zio.interop.catz._
//import scalaz.zio.interop.catz.implicits._
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe._
import org.http4s.headers.{Accept, `Content-Type`}

/*
TODO:
- sbt hardcore // see zio sbt :)
- Database: https://github.com/mschuwalow/zio-todo-backend/tree/develop/app/src/main/scala/com/schuwalow/zio/todo
  also https://github.com/edvmorango/edm-message-service-consumer/blob/master/src/main/scala/effects/repository/MessageRepository.scala
- Refresh token consideration
- The rest
- API ?
- metrics
- config using ciris // ZIO.fromEither(ConfigLoader.load)
- ability to push events somewhere?
 */

/*
https://drive.google.com/drive/folders/0B9nVzuTl4YMOaXAzRnRhdXVma1k
https://github.com/l3flo/jeedom-heatzy/blob/master/core/class/heatzy.class.php
 */


object Main extends CatsApp {
  val config = ConfigFactory.load()
  val heatzy = Heatzy(config.getString("heatzy.cloud.url"), config.getString("heatzy.app.id"))

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[TaskR[Console, ?], A] = jsonOf[TaskR[Console, ?], A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[TaskR[Console, ?], A] = jsonEncoderOf[TaskR[Console, ?], A]


  def post[T, A, B](path: String, body: A)(implicit d: EntityDecoder[TaskR[Console, ?], B], d2: EntityEncoder[TaskR[Console, ?], A]): TaskR[T with WebService with Console, B] = {
    val res = Request[TaskR[Console, ?]](
      method = Method.POST,
      uri = Uri.unsafeFromString(s"${heatzy.url}$path"),
      headers = Headers.of(
        Accept(MediaType.application.json),
        `Content-Type`(MediaType.application.json),
        Header("X-Gizwits-Application-Id", heatzy.appId)
      )
    ).withEntity(body)

    ZIO.accessM(_.webService.fetchAs[B](res))
  }

  def get[T, A](path: String, token: String)(implicit d: EntityDecoder[TaskR[Console, ?], A]): TaskR[T with WebService with Console, A] = {
    val res = Request[TaskR[Console, ?]](
      method = Method.GET,
      uri = Uri.unsafeFromString(s"${heatzy.url}$path"),
      headers = Headers.of(
        Accept(MediaType.application.json),
        `Content-Type`(MediaType.application.json),
        Header("X-Gizwits-Application-Id", heatzy.appId),
        Header("X-Gizwits-User-token", token)
      )
    )

    ZIO.accessM(_.webService.fetchAs[A](res))
  }

  def bindings(token: String): TaskR[WebService with Console, BindingsResponse] = {
    get[WebService with Console, BindingsResponse]("/bindings", token)
  }

  def login(username: String, password: String): TaskR[WebService with Console, LoginResponse] = {
    val body = LoginRequest(username, password)
    post[WebService with Console, LoginRequest, LoginResponse]("/login", body)
  }

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] = {
    val prog = for {
      l <- login("xxx@gmail.com", "pwd")
      b <- bindings(l.token)
      _ <- putStrLn(b.toString)
    } yield 0

    prog.provide(new WebServiceLive with Console.Live)
      .catchAll(t => putStrLn(t.getMessage) *> ZIO.succeedLazy(0))
  }
}
