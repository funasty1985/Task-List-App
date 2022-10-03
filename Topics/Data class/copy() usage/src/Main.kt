// do not change this data class
data class Box(val height: Int, val length: Int, val width: Int)

fun main() {
    // implement your code here
    val h = readln().toInt()
    val l1 = readln().toInt()
    val l2 = readln().toInt()
    val w = readln().toInt()

    val b1 = Box(h, l1, w)
    val b2 = b1.copy(length=l2)

    println(b1.toString())
    println(b2.toString())
}
