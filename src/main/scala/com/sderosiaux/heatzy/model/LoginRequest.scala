package com.sderosiaux.heatzy.model

case class LoginRequest(username: String, password: String, lang: String = "en")
