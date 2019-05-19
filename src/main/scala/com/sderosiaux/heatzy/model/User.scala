package com.sderosiaux.heatzy.model

case class User(
                 name: Option[String],
                 username: Option[String],
                 uid: String,
                 lang: String,
                 remark: Option[String],
                 is_anonymous: Boolean,
                 email: String
               )
