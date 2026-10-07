package com.hmc.listacompracompose.app

import android.app.Application
import com.hmc.listacompracompose.app.data.di.AppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.annotation.KoinApplication

import org.koin.plugin.module.dsl.startKoin

@KoinApplication(modules = [AppModule::class])
class ListaCompraApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin<ListaCompraApp> {
            androidContext(this@ListaCompraApp)
        }
    }
}
