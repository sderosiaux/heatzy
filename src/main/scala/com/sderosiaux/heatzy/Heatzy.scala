package com.sderosiaux.heatzy

//import cats.effect.Concurrent
import scalaz.zio._
import scalaz.zio.console._
import scalaz.zio.interop.catz._
//import scalaz.zio.interop.catz.implicits._
import cats.implicits._
import com.typesafe.config.ConfigFactory
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.headers.{Accept, `Content-Type`}

import scala.concurrent.ExecutionContext.Implicits.global

case class LoginRequest(username: String, password: String, lang: String = "en")

case class LoginResponse(token: String, uid: String, expire_at: Long)

case class Device(protoc: Int, is_disabled: Boolean, dev_alias: String)

case class BindingsResponse(devices: List[Device])

case class Heatzy(url: String, appId: String)

/*
TODO:
- sbt hardcore // see zio sbt :)
- ZIO (env)
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

object Heatzy extends CatsApp {
  val config = ConfigFactory.load()
  val heatzy = Heatzy(config.getString("heatzy.cloud.url"), config.getString("heatzy.app.id"))

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[Task, A] = jsonOf[Task, A]
  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[Task, A] = jsonEncoderOf[Task, A]

  val p: Option[String => ZIO[Console, Nothing, Unit]] = ((x: String) => putStrLn(x)).some

  def post[A, B](path: String, body: A)(implicit d: EntityDecoder[Task, B], d2: EntityEncoder[Task, A], client: Client[Task]): Task[B] = {
    val res = Request[Task](
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

  def get[A](path: String, token: String)(implicit client: Client[Task], d: EntityDecoder[Task, A]): Task[A] = {
    val res = Request[Task](
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

  def bindings(token: String)(implicit client: Client[Task]): Task[BindingsResponse] = {
    get[BindingsResponse]("/bindings", token)
  }

  def login(username: String, password: String)(implicit client: Client[Task]): Task[LoginResponse] = {
    val body = LoginRequest(username, password)
    post[LoginRequest, LoginResponse]("/login", body)
  }

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] = {
    val x = BlazeClientBuilder[Task](global).resource.use { implicit c =>
      //implicit val x = implicitly[Concurrent[TaskR[Environment, ?]]]

//      implicit val client: Client[TaskR[Environment, ?]] = ResponseLogger(logHeaders = true, logBody = true, logAction = None)(
//        RequestLogger(logHeaders = true, logBody = true, logAction = None)(c)
//      )

      val x: ZIO[Console, Throwable, Int] = for {
        l <- login("xxx@gmail.com", "pwd")
        b <- bindings(l.token)
        _ <- putStrLn(b.toString)
      } yield 0

      x.provide(Environment) // remove dependency upon Console
    }

    val y: ZIO[Environment, Nothing, Int] = x.foldM(ex => putStrLn("failed:" + ex.getMessage) *> ZIO.succeed(1), _ => putStrLn("OK") *> ZIO.succeed(0))
    y
  }
}
