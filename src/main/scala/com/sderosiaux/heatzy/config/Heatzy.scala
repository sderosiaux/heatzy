package com.sderosiaux.heatzy.config

case class Heatzy(url: String, appId: String)

trait HeatzyConfiguration {
  val config: Heatzy
}









