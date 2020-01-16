package dev.abhattacharyea.safety.model

import com.google.gson.annotations.SerializedName

data class Polyline(
	@SerializedName("points")
	val points: String = ""
)