package com.sderosiaux.heatzy

import com.sderosiaux.heatzy.config.{Heatzy, HeatzyConfiguration}
import com.sderosiaux.heatzy.webservice.{HeatzyWebService, Http4sHeatzyWebService}
import scalaz.zio.console.{Console, putStrLn}
import scalaz.zio.interop.catz._
import scalaz.zio.system.System
import scalaz.zio.{Task, ZIO}

/*
TODO:
- sbt hardcore // see zio sbt :)
- Database: https://github.com/mschuwalow/zio-todo-backend/tree/develop/app/src/main/scala/com/schuwalow/zio/todo
  also https://github.com/edvmorango/edm-message-service-consumer/blob/master/src/main/scala/effects/repository/MessageRepository.scala
- Refresh token consideration
- The rest
- API ?
- metrics
- ability to push events somewhere?
 */

/*
https://drive.google.com/drive/folders/0B9nVzuTl4YMOaXAzRnRhdXVma1k
https://github.com/l3flo/jeedom-heatzy/blob/master/core/class/heatzy.class.php
 */


object Main extends CatsApp {

  import HeatzyWebService._

  val heatzy: Task[Heatzy] = HeatzyConfiguration.load()

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] = {
    heatzy
      .flatMap(h => main(h).provideSome(appEnv(h)))
      .catchAll(t => putStrLn(t.getMessage) *> ZIO.succeedLazy(0)) // remove Throwable error channel
  }

  private def main(h: Heatzy): ZIO[Console with HeatzyWebService, Throwable, Int] = {
    for {
      l <- login(h.login, h.pwd)
      b <- bindings(l.token)
      _ <- putStrLn(b.toString)

      _ <- scheduler(b.devices.head.did, l.token).flatMap(x => putStrLn(x.toString))
      _ <- latest(b.devices.head.did, l.token).flatMap(x => putStrLn(x.toString))
      _ <- datapoint(b.devices.head.product_key, l.token).flatMap(x => putStrLn(x.toString))
      _ <- device(b.devices.head.did, l.token).flatMap(x => putStrLn(x.toString))
      _ <- user(l.token).flatMap(x => putStrLn(x.toString))
    } yield 0
  }

  def appEnv(cfg: Heatzy)(base: Environment): System with Console with HeatzyWebService = new System with Console with HeatzyWebService {
    override val console: Console.Service[Any] = base.console
    override val system: System.Service[Any] = base.system

    override def heatzy: HeatzyWebService.Service[Any] = new Http4sHeatzyWebService(runtime, cfg)
  }

}
