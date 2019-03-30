package com.examples.linak

import koma.end
import koma.mat

fun main() {
    val iHat = mat[0.0, 1.0]
    val jHat = mat[1.0, 0.0]
    val ijHat =
        mat[0.0, 1.0 end 1.0, 0.0]


    println(
        """Matrix of:
        |$iHat
        |+
        |$jHat
        |= ${iHat + jHat} == $ijHat
    """.trimMargin()
    )
}