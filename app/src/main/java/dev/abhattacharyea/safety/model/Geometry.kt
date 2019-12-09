package dev.abhattacharyea.safety.model

import com.google.gson.annotations.SerializedName

data class Geometry(
	@SerializedName("viewport")
	val viewport: Viewport?,
	@SerializedName("location")
	val location: Location?
)

