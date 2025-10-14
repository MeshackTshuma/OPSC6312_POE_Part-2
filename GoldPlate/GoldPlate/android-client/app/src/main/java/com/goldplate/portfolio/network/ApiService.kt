package com.goldplate.portfolio.network

import retrofit2.Response
import retrofit2.http.*

data class AuthResponse(val token: String?, val user: Map<String,Any>?)
data class SimpleResponse(val success: Boolean?, val error: String?)
data class PortfolioItem(val id: Int, val symbol: String, val shares: Double, val avg_price: Double?, val current_price: Double?)

interface ApiService {
    @POST("/auth/register")
    suspend fun register(@Body body: Map<String,String>): Response<AuthResponse>

    @POST("/auth/login")
    suspend fun login(@Body body: Map<String,String>): Response<AuthResponse>

    @GET("/portfolio")
    suspend fun getPortfolio(@Header("Authorization") auth: String): Response<Map<String, List<PortfolioItem>>>

    @POST("/portfolio")
    suspend fun addPortfolio(@Header("Authorization") auth: String, @Body body: Map<String,Any>): Response<SimpleResponse>

    @DELETE("/portfolio/{id}")
    suspend fun deletePortfolio(@Header("Authorization") auth: String, @Path("id") id: Int): Response<SimpleResponse>

    @GET("/user/settings")
    suspend fun getSettings(@Header("Authorization") auth: String): Response<Map<String,Any>>

    @POST("/user/settings")
    suspend fun setSettings(@Header("Authorization") auth: String, @Body body: Map<String,Any>): Response<Map<String,Any>>
}
