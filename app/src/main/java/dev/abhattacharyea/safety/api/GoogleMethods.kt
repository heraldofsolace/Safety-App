package dev.abhattacharyea.safety.api

import dev.abhattacharyea.safety.model.Directions
import dev.abhattacharyea.safety.model.NearbySearch
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GoogleMethods {
	@GET("places")
	fun getNearbySearch(
		@Query("location") location: String,
		@Query("radius") radius: String,
		@Query("type") types: String,
		@Header("Authorization") token: String
//		@Query("key") key: String
	): Call<NearbySearch>
	
	@GET("directions")
	fun getDirections(
		@Query("origin") origin: String,
		@Query("destination") destination: String,
		@Header("Authorization") token: String
//		@Query("key") key: String
	): Call<Directions>
}