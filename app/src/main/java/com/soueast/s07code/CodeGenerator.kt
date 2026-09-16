package com.soueast.s07code

import java.util.Locale

/**
 * Код для ГУ Soueast S07 awd по текущему месяцу, дню и часу.
 *
 * Формат жёстко привязан к [Locale.US]: String.format без локали на части локалей
 * (ar, fa, ряд индийских) подставляет не-ASCII цифры, из-за чего toLong() падал
 * с NumberFormatException прямо при запуске приложения.
 */
fun generateCode(month: Int, day: Int, hour: Int): String {
    val s = String.format(Locale.US, "%02d%02d%02d", month, day, hour)
    val num = s.toLong() * 240830L
    val code = num % 1000000L
    return String.format(Locale.US, "%06d", code)
}
