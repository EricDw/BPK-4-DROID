package com.examples.linak

typealias Vector = DoubleArray

fun row(vararg x: Double): Vector = doubleArrayOf(*x)

infix fun Vector.scaleBy(other: Vector): Vector =
    if (size == other.size)
        mapIndexed { index, d ->
            d scaleBy other[index]
        }.toDoubleArray()
    else throw ColumnSizeException()
