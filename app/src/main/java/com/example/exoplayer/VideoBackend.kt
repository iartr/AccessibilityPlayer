package com.example.exoplayer

import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

enum class VideoActionsDTO {
    @SerializedName("blur")
    BLUR,

    @SerializedName("screamer")
    SCRIMER,

    @SerializedName("lowerContrast")
    LOWER_CONTRAST,

    @SerializedName("lowerSaturation")
    LOWER_SATURATION,
}

data class VideoConfig(
    @SerializedName("name")
    val name: String,
    @SerializedName("data")
    val data: List<DataEntity>
)

data class DataEntity(
    @SerializedName("startTime")
    val startTime: Long,
    @SerializedName("endTime")
    val endTime: Long,
    @SerializedName("actions")
    val actions: List<VideoActionsDTO>
)

interface VideoBackend {
    @GET("v1/videos/{videoName}")
    fun getConfig(
        @Path("videoName")
        videoName: String
    ): Single<VideoConfig>
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://c6e8-178-70-176-177.eu.ngrok.io/")
    .client(
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    )
    .addConverterFactory(GsonConverterFactory.create())
    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
    .build()
    .create(VideoBackend::class.java)