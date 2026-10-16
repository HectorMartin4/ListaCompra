package com.hmc.listacompracompose.app.util

fun String.normalizeName(): String =
    trim().replace(Regex("\\s+"), " ").replaceFirstChar { it.uppercase() }

fun Double?.formatPrice(): String = this?.let { "%.2f €".format(it) } ?: "Sin precio"