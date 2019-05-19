package com.sderosiaux.heatzy.model

case class UIntSpec(
                     min: Int,
                     max: Int,
                     ratio: Int,
                     addition: Int
                   )

case class Attr(
                 display_name: String,
                 name: String,
                 data_type: String,
                 enum: Option[List[String]],
                 // position
                 `type`: String,
                 id: Int,
                 desc: String,
                 uint_spec: Option[UIntSpec]
               )

case class Entity(
                   id: Int,
                   name: String,
                   display_name: String,
                   attrs: List[Attr]
                 )

case class DataPoint(
                      name: String,
                      entities: List[Entity],
                      protocolType: String,
                      product_key: String,
                      packetVersion: String
                    )
