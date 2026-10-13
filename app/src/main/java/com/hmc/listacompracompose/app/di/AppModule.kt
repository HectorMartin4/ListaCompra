package com.hmc.listacompracompose.app.di

import com.hmc.listacompracompose.features.di.FeatureModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(
    includes = [
        LocalModule::class,
        RemoteModule::class,
        FeatureModule::class,
    ]
)
@ComponentScan("com.hmc.listacompracompose")
class AppModule