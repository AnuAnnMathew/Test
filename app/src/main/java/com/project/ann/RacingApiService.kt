package com.project.ann

import com.google.gson.JsonObject
import com.project.ann.model.ResponseModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface RacingApiService {
    @GET("/rest/v1/racing/")
    fun getNextRaces(
        @Query("method") method: String,
        @Query("count") count: Int
    ): Call<JsonObject>
}