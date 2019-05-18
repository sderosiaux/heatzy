package com.sderosiaux.heatzy

import com.sderosiaux.heatzy.config.Heatzy
import com.sderosiaux.heatzy.model.{BindingsResponse, LoginRequest, LoginResponse}
import com.sderosiaux.heatzy.webservice.{HeatzyWebService, HeatzyWebServiceLive}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}
import scalaz.zio.{ZIO, _}
import scalaz.zio.console.{Console, putStrLn}
import scalaz.zio.interop.catz._
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}

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

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[Task, A] = jsonOf[Task, A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[Task, A] = jsonEncoderOf[Task, A]


  def bindings(token: String): ZIO[HeatzyWebService, Throwable, BindingsResponse] = {
    ZIO.accessM((x: HeatzyWebService) => {
      x.heatzy.get[BindingsResponse]("/bindings", token)
    })
  }

  def login(username: String, password: String): ZIO[HeatzyWebService, Throwable, LoginResponse] = {
    val body = LoginRequest(username, password)
    ZIO.accessM((x: HeatzyWebService) => {
      x.heatzy.post[LoginRequest, LoginResponse]("/login", body)
    })
  }

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] = {
    val prog: ZIO[Console with HeatzyWebService, Throwable, Int] = for {
      l <- login("xxx@gmail.com", "pwd")
      b <- bindings(l.token)
      _ <- putStrLn(b.toString)
    } yield 0

    val env = new Console.Live with HeatzyWebService {
      override def heatzy: HeatzyWebService.Service[Any] = HeatzyWebServiceLive
    }


    prog.provide(env)
      .catchAll(t => putStrLn(t.getMessage) *> ZIO.succeedLazy(0))
  }
}
