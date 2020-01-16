package dev.abhattacharyea.safety.model

import com.google.gson.annotations.SerializedName

data class OverviewPolyline(
	@SerializedName("points")
	val points: String = ""
)