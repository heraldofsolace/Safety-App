package dev.abhattacharyea.safety.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
	private const val GOOGLE_BASE_URL = "https://us-central1-safety-12.cloudfunctions.net/"
	
	fun googleMethods(): GoogleMethods {
		val retrofit = Retrofit.Builder()
			.baseUrl(GOOGLE_BASE_URL)
			.client(OkHttpClient().newBuilder().build())
			.addConverterFactory(GsonConverterFactory.create())
			.build()
		
		return retrofit.create(GoogleMethods::class.java)
	}
}