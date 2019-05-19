package com.sderosiaux.heatzy

import com.sderosiaux.heatzy.model.{BindingsResponse, DataPoint, Device, DeviceStatus, Scheduler, User}
import io.circe.generic.auto._
import io.circe.parser._
import org.scalatest.{EitherValues, FlatSpec, Matchers}

import scala.io.Source

class SerdeTest extends FlatSpec with Matchers with EitherValues {
  "bindings" should "work" in {
    decode[BindingsResponse](res("bindings.json")) should be ('right)
  }

  "schedulers" should "work" in {
    decode[List[Scheduler]](res("schedulers_empty.json")) should be ('right)
  }

  "status" should "work" in {
    decode[DeviceStatus](res("status.json")) should be ('right)
  }

  "datapoint" should "work" in {
    decode[DataPoint](res("datapoint.json")) should be ('right)
  }

  "device" should "work" in {
    decode[Device](res("device.json")) should be ('right)
  }

  "user" should "work" in {
    decode[User](res("user.json")) should be ('right)
  }

  private def res(filename: String): String = {
    Source.fromResource(filename).getLines().mkString("\n")
  }
}
