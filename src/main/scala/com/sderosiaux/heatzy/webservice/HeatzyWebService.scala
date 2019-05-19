package com.sderosiaux.heatzy.webservice

import com.sderosiaux.heatzy.model.{BindingsResponse, LoginResponse}
import scalaz.zio.{Task, TaskR, ZIO}

trait HeatzyWebService extends Serializable {
  def heatzy: HeatzyWebService.Service[Any]
}
//
//trait Serde[A] {
//  type Format
//  def serialize(item: A): Task[Format]
//  def deserialize(json: Format): Task[A]
//}

//
//    def post[A, B](path: String, body: A): TaskR[R with Serde[A] with Serde[B], B]
//    def get[A](path: String, token: String): TaskR[R with Serde[A], A]
//

object HeatzyWebService extends Serializable {

  trait Service[R] extends Serializable {
    def bindings(token: String): Task[BindingsResponse]
    def login(username: String, password: String): Task[LoginResponse]
  }

  def bindings(token: String): TaskR[HeatzyWebService, BindingsResponse] = {
    ZIO.accessM[HeatzyWebService](_.heatzy.bindings(token))
  }

  def login(username: String, password: String): TaskR[HeatzyWebService, LoginResponse] = {
    ZIO.accessM[HeatzyWebService](_.heatzy.login(username, password))
  }
}
