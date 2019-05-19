package com.sderosiaux.heatzy.config

case class Heatzy(url: String, appId: String, login: String, pwd: String)

trait HeatzyConfiguration {
  val config: Heatzy
}









