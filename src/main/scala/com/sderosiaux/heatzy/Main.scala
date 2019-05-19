package com.sderosiaux.heatzy

import com.sderosiaux.heatzy.config.{Heatzy, HeatzyConfiguration}
import com.sderosiaux.heatzy.webservice.{HeatzyWebService, Http4sHeatzyWebService}
import scalaz.zio.console.{Console, putStrLn}
import scalaz.zio.interop.catz._
import scalaz.zio.system.System
import scalaz.zio.{Task, ZIO}
import pureconfig.generic.auto._

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

  val heatzy: Task[Heatzy] = ZIO.fromEither(pureconfig.loadConfig[Heatzy]("heatzy"))
    .catchAll(failures => ZIO.fail(new Exception(failures.toList.map(_.description).mkString(","))))

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] = {
    val prog: ZIO[Console with HeatzyWebService, Throwable, Int] = for {
      h <- heatzy
      l <- login(h.login, h.pwd)
      b <- bindings(l.token)
      _ <- putStrLn(b.toString)
    } yield 0

    val zio = heatzy.flatMap { h => prog.provideSome(appEnv(h)) }
    zio.catchAll(t => putStrLn(t.getMessage) *> ZIO.succeedLazy(0)) // remove Throwable error channel
  }

  def appEnv(cfg: Heatzy)(base: Environment): System with Console with HeatzyWebService = new scalaz.zio.system.System with Console with HeatzyWebService {
    override val console: Console.Service[Any] = base.console
    override val system: System.Service[Any] = base.system

    override def heatzy: HeatzyWebService.Service[Any] = new Http4sHeatzyWebService(runtime, new HeatzyConfiguration {
      override val config: Heatzy = cfg
    })
  }

}
