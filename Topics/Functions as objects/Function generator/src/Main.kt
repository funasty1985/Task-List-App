// TODO: provide three functions here

fun identity(arg: Int) = arg
fun half(arg: Int) = (arg * 0.5).toInt()
fun zero(arg: Int) = 0

fun generate(functionName: String): (Int) -> Int {
    // TODO: provide implementation here
    return when (functionName) {
        "identity" -> ::identity
        "half" -> ::half
        else -> ::zero
    }
}
