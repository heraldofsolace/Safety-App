package dev.abhattacharyea.safety.model

data class Spot(
	val name: String = "",
	val lat: Double?,
	val lng: Double?,
	val icon: String?,
	val photoReference: String?
)