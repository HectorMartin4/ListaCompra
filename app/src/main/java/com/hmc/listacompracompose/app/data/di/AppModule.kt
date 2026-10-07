package com.hmc.listacompracompose.app.data.di

import com.hmc.listacompracompose.features.items.di.ItemModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    includes = [
        LocalModule::class,
        RemoteModule::class,
        ItemModule::class,
    ]
)
@ComponentScan("com.hmc.listacompracompose")
class AppModule