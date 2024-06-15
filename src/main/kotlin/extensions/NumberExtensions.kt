package extensions

import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

fun Number.compact(): String = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
    maximumFractionDigits = 0
    roundingMode = RoundingMode.HALF_UP
}.format(this)

fun Float.roundToDigits(digits: Int = 1) = this.toBigDecimal().setScale(digits, RoundingMode.HALF_UP).toFloat()

fun <T>List<T>.sumOfNotNull(selector: (T) -> Number?): Int {
    return this.mapNotNull(selector).sumOf { it.toInt() }
}

fun <T>List<T>.maxOfNotNull(selector: (T) -> Number?): Int {
    return this.mapNotNull(selector).maxOf { it.toInt() }
}
