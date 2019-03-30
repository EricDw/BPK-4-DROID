package com.examples.linak

fun main() {

    val basisMatrix = matrixOfNRows(
        row(0.0, 1.0),
        row(1.0, 0.0)
    )

    val threeByThreeIdentityMatrix = matrixOfNRows(
        row(1.0, 0.0, 0.0),
        row(0.0, 1.0, 0.0),
        row(0.0, 0.0, 1.0)
    )

    val twoByTwoIdentityMatrix = matrixOfNRows(
        row(1.0, 0.0, 0.0),
        row(0.0, 1.0, 0.0),
        row(0.0, 0.0, 1.0)
    )

    val practiceMatrix = matrixOfNRows(
        row(11.0, 12.0),
        row(13.0, 14.0),
        row(15.0, 16.0)
    )
    val practiceMatrix2 = matrixOfNRows(
        row(1.0, 2.0, 3.0),
        row(4.0, 5.0, 6.0)
    )

    println(threeByThreeIdentityMatrix.getRow(0).toMatrix())
    println(threeByThreeIdentityMatrix.getRow(1).toMatrix())
    println(threeByThreeIdentityMatrix.getRow(2).toMatrix())
    println()
    println(threeByThreeIdentityMatrix.getRows())
    println()
    println("Input ==\n$practiceMatrix\n&\n$threeByThreeIdentityMatrix")
    println()
    println("\nOutput ==\n${practiceMatrix * twoByTwoIdentityMatrix}")


//    val product = diagonalLine * threeByThreeIdentityMatrix
//    val correctOrNot: String = if (product == diagonalLine) "correct" else "not correct"
//    println(
//        """Matrix of:
//        |$diagonalLine
//        |*
//        |$threeByThreeIdentityMatrix
//        | expected is:
//        |$diagonalLine
//        | product =
//        |$product
//        |Product is $correctOrNot
//    """.trimMargin()
//    )


}