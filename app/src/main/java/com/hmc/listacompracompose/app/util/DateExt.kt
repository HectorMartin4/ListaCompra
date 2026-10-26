package com.hmc.listacompracompose.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Date

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

fun Date.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate()

fun Date.formatDate(): String = toLocalDate().format(dateFormatter)

// "Hoy", "Ayer" o la fecha completa (también para fechas futuras)
fun Date.formatRelativeDate(): String =
    when (ChronoUnit.DAYS.between(toLocalDate(), LocalDate.now())) {
        0L -> "Hoy"
        1L -> "Ayer"
        else -> formatDate()
    }

// El DatePicker devuelve medianoche UTC; la convertimos al mediodía local del mismo
// día para que se muestre la fecha correcta en cualquier zona horaria
fun utcMidnightToLocalDayMillis(utcMillis: Long): Long =
    Instant.ofEpochMilli(utcMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .atTime(12, 0)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

// Inicio (inclusive) y fin (exclusive) del día local que contiene epochMillis,
// para acotar consultas y reemplazos "un precio por día"
fun dayRange(epochMillis: Long): Pair<Long, Long> {
    val zone = ZoneId.systemDefault()
    val day = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
    return day.atStartOfDay(zone).toInstant().toEpochMilli() to
            day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
}