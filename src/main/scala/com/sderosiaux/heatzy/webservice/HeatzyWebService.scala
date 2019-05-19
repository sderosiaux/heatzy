package com.sderosiaux.heatzy.webservice

import com.sderosiaux.heatzy.model.{BindingsResponse, DataPoint, Device, DeviceStatus, LoginResponse, Scheduler, User}
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

    def scheduler(did: String, token: String): Task[List[Scheduler]]

    def latest(did: String, token: String): Task[DeviceStatus]

    def datapoint(did: String, token: String): Task[DataPoint]

    def device(did: String, token: String): Task[Device]

    def user(token: String): Task[User]

  }

  def bindings(token: String): TaskR[HeatzyWebService, BindingsResponse] = {
    ZIO.accessM[HeatzyWebService](_.heatzy.bindings(token))
  }

  def login(username: String, password: String): TaskR[HeatzyWebService, LoginResponse] = {
    ZIO.accessM[HeatzyWebService](_.heatzy.login(username, password))
  }

  def scheduler(did: String, token: String): TaskR[HeatzyWebService, List[Scheduler]] = {
    ZIO.accessM[HeatzyWebService](_.heatzy.scheduler(did, token))
  }

  def latest(did: String, token: String): TaskR[HeatzyWebService, DeviceStatus] = {
    ZIO.accessM[HeatzyWebService](_.heatzy.latest(did, token))
  }

  def datapoint(productKey: String, token: String): TaskR[HeatzyWebService, DataPoint] = {
    ZIO.accessM[HeatzyWebService](_.heatzy.datapoint(productKey, token))
  }

  def device(did: String, token: String): TaskR[HeatzyWebService, Device] = {
    ZIO.accessM[HeatzyWebService](_.heatzy.device(did, token))
  }

  def user(token: String): TaskR[HeatzyWebService, User] = {
    ZIO.accessM[HeatzyWebService](_.heatzy.user(token))
  }
}
