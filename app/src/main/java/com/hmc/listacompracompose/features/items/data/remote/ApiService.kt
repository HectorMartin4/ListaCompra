package com.hmc.listacompracompose.features.items.data.remote

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("api/products")
    suspend fun getItemList(): Response<List<ItemApiModel>>

    @DELETE("api/products/{id}")
    suspend fun deleteItem(
        @Path("id") id: Int
    ): Response<Unit>
}