package com.sderosiaux.heatzy.model

case class LoginResponse(token: String, uid: String, expire_at: Long)
