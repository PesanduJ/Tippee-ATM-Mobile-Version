package com.example.tippee_atm_mobile_version

import org.opencv.core.Point
import java.io.Serializable

class SerializablePoint(val x: Double, val y: Double) : Serializable {
    fun toPoint(): Point {
        return Point(x, y)
    }
}