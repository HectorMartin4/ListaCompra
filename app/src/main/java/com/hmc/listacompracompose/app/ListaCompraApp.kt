package com.hmc.listacompracompose.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ListaCompraApp: Application() {

    override fun onCreate(){
        super.onCreate()
        startKoin {
            androidContext(this@ListaCompraApp)
        }
    }
}