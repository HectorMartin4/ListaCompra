package com.hmc.listacompracompose.app

import android.app.Application
import com.hmc.listacompracompose.features.items.di.ItemModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class ListaCompraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ListaCompraApp)
            modules(
                ItemModule().module
            )
        }
    }
}
