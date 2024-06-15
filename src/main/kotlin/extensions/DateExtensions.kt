package extensions

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

val INVALID_DATE = Date(0L)
val EMPTY_RANGE = DateRange(INVALID_DATE, INVALID_DATE)
const val MILLISECONDS_IN_DAY: Float = 86400000f
const val LAST_HOUR = 23
const val LAST_MINUTE = 59
const val LAST_SECOND = 59

data class DateRange (
    val startDate: Date = INVALID_DATE,
    val endDate: Date = INVALID_DATE
)

enum class WeekDay {
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY
}

const val DATE_FORMAT_MONTH_DAY = "MMM dd"
const val DATE_FORMAT_MONTH_DAY_NO_LEADING_ZEROS = "MMM d"
const val DATE_FORMAT_MONTH_YEAR = "MMM yyyy"
const val DATE_FORMAT_MONTH_DAY_YEAR = "MMM dd, yyyy"
const val DATE_FORMAT_MONTH_DAY_YEAR_NO_LEADING_ZEROS = "MMM d, yyyy"
const val DATE_FORMAT_MONTH = "MMM"
const val DATE_FORMAT_MONTH_LONG = "MMMM"

fun Date.isStartOfRange(range: DateRange) = this != INVALID_DATE && range.startDate == this
fun Date.isEndOfRange(range: DateRange) = this != INVALID_DATE && range.endDate == this
fun Date.isInRange(range: DateRange) = this.after(range.startDate) && this.before(range.endDate)
fun DateRange.isComplete(): Boolean = this.startDate != INVALID_DATE && this.endDate != INVALID_DATE && this.endDate != this.startDate

fun Date.onOrBefore(other: Date?): Boolean = this == (other ?: Date()) || this.before(other ?: Date())
fun Date.onOrAfter(other: Date?): Boolean = this == (other ?: Date()) || this.after(other ?: Date())

fun Date.monthName(): String = SimpleDateFormat(DATE_FORMAT_MONTH, Locale.getDefault()).format(this)
fun Date.monthNameLong(): String = SimpleDateFormat(DATE_FORMAT_MONTH_LONG, Locale.getDefault()).format(this)
fun Date.monthNameAndDay(): String = SimpleDateFormat(DATE_FORMAT_MONTH_DAY,Locale.getDefault()).format(this)
fun Date.monthDayAndYear():String = SimpleDateFormat(DATE_FORMAT_MONTH_DAY_YEAR,Locale.getDefault()).format(this)
fun Date.hoursOfDay(): Int {
    val cal = Calendar.getInstance()
    cal.time = this

    return cal.get(Calendar.HOUR_OF_DAY)
}

fun Date.startOfDay(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    return cal.time
}

fun Date.isToday(): Boolean = abs(Date().startOfDay().time - this.startOfDay().time) < 1000

fun Date.isSameDayAs(date: Date): Boolean {
    val calendarA = Calendar.getInstance().apply { time = this@isSameDayAs }
    val calendarB = Calendar.getInstance().apply { time = date }

    return calendarA.get(Calendar.YEAR) == calendarB.get(Calendar.YEAR)
            && calendarA.get(Calendar.DAY_OF_YEAR) == calendarB.get(Calendar.DAY_OF_YEAR)
}

fun Date.isFirstOfMonth(): Boolean {
    val cal = Calendar.getInstance().apply { time = this@isFirstOfMonth }
    return cal.get(Calendar.DAY_OF_MONTH) == 1
}

const val MIDDAY = 12
const val START_DAY = 0
fun Date.shortTime(): String {
    val cal = Calendar.getInstance()
    cal.time = this
    val timeInstance = LocalDateTime.ofInstant(Instant.ofEpochMilli(cal.timeInMillis), ZoneId.systemDefault())
    return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(timeInstance)
}

fun Date.monthsSince(other: Date): Int {
    val monthsBetween = ChronoUnit.MONTHS.between(other.toYearMonth(), this.toYearMonth()).toInt()
    bcprint("$this.monthsSince($other) = between(${other.toYearMonth()}, ${this.toYearMonth()}) = $monthsBetween (using ${ZoneId.systemDefault()})")
    return monthsBetween
}

fun Date.toYearMonth(): YearMonth = Calendar.getInstance().apply { time = this@toYearMonth }.let {
    val year = it.get(Calendar.YEAR)
    val month = it.get(Calendar.MONTH) + 1
    YearMonth.of(year, month)
}

operator fun Date.plus(days: Int): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.add(Calendar.DAY_OF_YEAR, days)

    return cal.time
}

fun Date.calendar(): Calendar = Calendar.getInstance().apply { time = this@calendar }

operator fun Date.minus(days: Int): Date = this.plus(-1 * days)
fun Date.addMonths(i: Int): Date = calendar().apply { add(Calendar.MONTH, i) }.time
fun Date.addYears(i: Int): Date = calendar().apply { add(Calendar.YEAR, i) }.time
fun Date.setYearTo(year: Int): Date = calendar().apply { set(Calendar.YEAR, year) }.time
fun Date.setYearAndMonthTo(date: Date): Date = calendar().apply {
    set(Calendar.YEAR, date.year())
    set(Calendar.MONTH, date.month())
}.time

fun Date.dayOfMonth(): Int = calendar().get(Calendar.DAY_OF_MONTH)

fun Date.firstDateOfYear(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.DAY_OF_YEAR, 1)

    return cal.time
}

fun Date.lastDateOfYear(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.DAY_OF_YEAR, 1)
    cal.add(Calendar.YEAR, 1)
    cal.add(Calendar.DAY_OF_YEAR, -1)

    return cal.time
}

fun Date.firstDateOfMonth(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.DAY_OF_MONTH, 1)

    return cal.time
}

fun Date.lastDateOfMonth(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.add(Calendar.MONTH, 1)
    cal.add(Calendar.DAY_OF_MONTH, -1)

    return cal.time
}

fun Date.firstDateOfWeek(): Date {
    val cal = Calendar.getInstance(Locale.US) // Set calendar to US locale as this is the locale of the date object being used
    cal.time = this
    cal.set(Calendar.DAY_OF_WEEK, 1)

    return cal.time
}

fun Date.lastDateOfWeek(): Date {
    val cal = Calendar.getInstance(Locale.US) // Set calendar to US locale as this is the locale of the date object being used
    cal.time = this
    cal.set(Calendar.DAY_OF_WEEK, 1)
    cal.add(Calendar.WEEK_OF_YEAR, 1)
    cal.add(Calendar.DAY_OF_WEEK, -1)

    return cal.time
}

fun Calendar.dayOfWeek(): WeekDay = when (get(Calendar.DAY_OF_WEEK)) {
    Calendar.SUNDAY -> WeekDay.SUNDAY
    Calendar.MONDAY -> WeekDay.MONDAY
    Calendar.TUESDAY -> WeekDay.TUESDAY
    Calendar.WEDNESDAY -> WeekDay.WEDNESDAY
    Calendar.THURSDAY -> WeekDay.THURSDAY
    Calendar.FRIDAY -> WeekDay.FRIDAY
    Calendar.SATURDAY -> WeekDay.SATURDAY
    else -> throw NotImplementedError("UNSUPPORTED DAY OF WEEK")
}

fun Date.dayOfWeek(): WeekDay = calendar().dayOfWeek()

fun Date.year(): Int = calendar().get(Calendar.YEAR)
fun Date.monthAndDay(leadingZeros: Boolean = true) = when (leadingZeros) {
    true -> formatWithLocalizedPattern(DATE_FORMAT_MONTH_DAY)
    false -> formatWithLocalizedPattern(DATE_FORMAT_MONTH_DAY_NO_LEADING_ZEROS)
}
fun Date.formatWithLocalizedPattern(pattern: String): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}
fun Date.month(): Int = calendar().get(Calendar.MONTH)
fun Date.monthAndYear(): String = SimpleDateFormat(DATE_FORMAT_MONTH_YEAR, Locale.getDefault()).format(this)
fun Date.standardFormat(): String = DateFormat.getDateInstance(DateFormat.SHORT).format(this)
fun Date.mediumFormat(): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(this)
fun Date.monthDayYear(leadingZeros: Boolean = true): String = when (leadingZeros) {
    true -> SimpleDateFormat(DATE_FORMAT_MONTH_DAY_YEAR, Locale.getDefault()).format(this)
    false -> SimpleDateFormat(DATE_FORMAT_MONTH_DAY_YEAR_NO_LEADING_ZEROS, Locale.getDefault()).format(this)
}

fun WeekDay.onOrBefore(other: WeekDay): Boolean = this.ordinal <= other.ordinal

fun Date.getDaysInMonthByDayOfWeek(dayOfWeek: WeekDay): List<Date> {
    val cal = calendar()
    return (0 until cal.getActualMaximum(Calendar.DAY_OF_MONTH)).mapNotNull {
        cal.set(Calendar.DAY_OF_MONTH, it + 1)
        if (cal.dayOfWeek() == dayOfWeek) cal.time else null
    }
}

fun DateRange.isValid(minDate: Date, maxDate: Date): Boolean = (startDate.onOrAfter(minDate) && endDate.onOrBefore(maxDate))
fun DateRange.makeValid(): DateRange = if (endDate != INVALID_DATE && endDate.before(startDate)) DateRange(endDate, startDate) else this

fun List<Date>.spansMultipleYears(): Boolean = minOf { it.year() } < maxOf { it.year() }
fun DateRange.spansMultipleYears(): Boolean = listOf(startDate, endDate).spansMultipleYears()

fun Date.endOfDay(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, LAST_HOUR)
    cal.set(Calendar.MINUTE, LAST_MINUTE)
    cal.set(Calendar.SECOND, LAST_SECOND)

    return cal.time
}

// e.g. 2023-06-20T19:19:12.314Z
// use SimpleDateFormat
fun String.parseJiraDate(): Date? {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    return try { formatter.parse(this) } catch (e: Throwable) {
        try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(this.split(".").first())
        } catch (e: Throwable) {
            bcprint("Unable to parse date: $this")
            throw e
        }
    }
}

fun beginningOfLastMonth(): Date {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, -1)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    return cal.time
}

fun endOfLastMonth(): Date {
    val cal = Calendar.getInstance()
    cal.add(Calendar.MONTH, -1)
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    return cal.time
}

fun beginningOfThisMonth(): Date {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    return cal.time
}

fun endOfThisMonth(): Date = Date()

fun Date.beginningOfWeek(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

fun Date.simpleFormat(): String = SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(this)
fun Date.numberFormat(): String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(this)

fun Date.weekString(): String = simpleFormat()

fun Date.weekString2(): String {
    val cal = Calendar.getInstance()
    cal.time = this
    val week = cal.get(Calendar.WEEK_OF_YEAR)
    val year = cal.get(Calendar.YEAR)
    return "$year-$week"
}

val jiraFormats = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
    "yyyy-MM-dd"
)

fun String.parseWithFormats(formats: List<String> = jiraFormats): Date? {
    formats.forEach { format ->
        try {
            return SimpleDateFormat(format, Locale.getDefault()).parse(this)
        } catch (e: Throwable) {
            bcprint("Unable to parse date: $this with format: $format")
        }
    }
    throw IllegalArgumentException("Unable to parse date: $this with any of the provided formats")
}

fun Long?.toDate(): Date = this?.let { Date(it) } ?: Date(0L)