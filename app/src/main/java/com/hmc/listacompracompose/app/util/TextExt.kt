package com.hmc.listacompracompose.app.util

fun String.normalizeName(): String =
    trim().replace(Regex("\\s+"), " ").replaceFirstChar { it.uppercase() }