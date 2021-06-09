package com.priyanshumaurya8868.priyanshu_android_task.models

import com.priyanshumaurya8868.priyanshu_android_task.util.Constant


data class Appointment(
    var time : String,
    var state : Int = Constant.ENABLE
)