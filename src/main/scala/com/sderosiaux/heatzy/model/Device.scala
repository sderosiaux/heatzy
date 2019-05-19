package com.sderosiaux.heatzy.model

case class Device(
                   did: String,
                   mac: String,
                   protoc: Int,
                   is_disabled: Boolean,
                   dev_alias: Option[String],
                   product_key: String,
                   is_online: Boolean,
                   is_sandbox: Option[Boolean],
                   passcode: String,
                   host: String,
                   port: Int,
                   port_s: Int,
                   ws_port: Int,
                   wss_port: Int,
                   remark: String,
                   `type`: Option[String],
                   proto_ver: Option[String],
                   role: String
                 )
