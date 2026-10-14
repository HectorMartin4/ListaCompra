package com.hmc.listacompracompose.features.items.data.remote

import com.google.gson.annotations.SerializedName

data class ItemApiModel(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double?
)
