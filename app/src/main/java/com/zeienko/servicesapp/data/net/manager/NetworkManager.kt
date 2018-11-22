package com.zeienko.servicesapp.data.net.manager

import com.google.gson.GsonBuilder
import com.zeienko.servicesapp.BuildConfig
import com.zeienko.servicesapp.data.net.api.UnsplashApi
import com.zeienko.servicesapp.domain.abstractions.ContentLoaderCallback
import com.zeienko.servicesapp.domain.abstractions.ControllerCallback
import com.zeienko.servicesapp.domain.interactors.ProgressListenerImp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object NetworkManager {

    private var unsplashApi: UnsplashApi

    init {
        val okHttpClient = initClient()
        unsplashApi = getApi(okHttpClient, UnsplashApi::class.java)
    }

    private fun initClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addNetworkInterceptor { chain ->
                val original = chain.request()
                val request = original
                    .newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private fun initClientProgressListener(callback: ContentLoaderCallback, controllerCallback: ControllerCallback): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                originalResponse.newBuilder()
                    .body(
                        ProgressResponseBodyWrapper(
                            originalResponse.body(),
                            ProgressListenerImp(callback, controllerCallback)
                        )
                    )
                    .build()
            }
            .build()
    }

    private fun <T> getApi(okHttpClient: OkHttpClient, tClass: Class<T>): T {
        val gson = GsonBuilder()
            .create()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(tClass)
    }

    fun getUnsplashApiWithProgressListener(callback: ContentLoaderCallback, controllerCallback: ControllerCallback): UnsplashApi {
        val client = initClientProgressListener(callback, controllerCallback)
        unsplashApi = getApi(client, UnsplashApi::class.java)
        return unsplashApi
    }

    fun getUnsplashApi(): UnsplashApi {
        return unsplashApi
    }

}