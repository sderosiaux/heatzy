package com.sderosiaux.heatzy.config

import pureconfig.generic.auto._
import scalaz.zio.{Task, ZIO}

case class Heatzy(url: String, appId: String, login: String, pwd: String)

object HeatzyConfiguration {
  def load(): Task[Heatzy] = {
    ZIO.fromEither(pureconfig.loadConfig[Heatzy]("heatzy"))
      .catchAll(failures => ZIO.fail(new Exception(failures.toList.map(_.description).mkString(","))))
  }
}








