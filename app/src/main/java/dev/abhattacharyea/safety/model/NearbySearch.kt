package dev.abhattacharyea.safety.model

import com.google.gson.annotations.SerializedName

data class NearbySearch(
	@SerializedName("next_page_token")
	val nextPageToken: String = "",
	@SerializedName("results")
	val results: List<ResultsItem>?,
	@SerializedName("status")
	val status: String = "",
	@SerializedName("error_message")
	val errorMessage: String? = ""
)