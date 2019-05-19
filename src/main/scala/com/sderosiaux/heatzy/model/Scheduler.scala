package com.sderosiaux.heatzy.model

case class Scheduler(
                      id: String,
                      date: String,
                      time: String,
                      repeat: String,
                      start_date: String,
                      end_date: String,
                      enabled: Boolean,
                      remark: String,
                      created_at: String
                    )
