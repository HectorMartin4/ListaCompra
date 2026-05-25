package com.hmc.listacompracompose.features.items.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {

    @GET("api/products")
    suspend fun getItemList(): Response<List<ItemApiModel>>
}