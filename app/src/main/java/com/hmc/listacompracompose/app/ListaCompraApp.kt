package com.hmc.listacompracompose.app

import android.app.Application

import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinApplication
import org.koin.plugin.module.dsl.startKoin

@KoinApplication
@ComponentScan("com.hmc.listacompracompose")
class ListaCompraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin<ListaCompraApp> {
            androidContext(this@ListaCompraApp)
        }
    }
}