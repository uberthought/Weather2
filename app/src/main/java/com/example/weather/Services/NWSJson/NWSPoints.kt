import com.google.gson.annotations.SerializedName

// https://www.json2kotlin.com/

class NWSPoints {
	data class Base(
//		@SerializedName("@context") val context: List<String>,
		@SerializedName("type") val type: String,
		@SerializedName("features") val features: List<Features>,
		@SerializedName("observationStations") val observationStations: List<String>
	)

	data class Features(
		@SerializedName("id") val id: String,
		@SerializedName("type") val type: String,
		@SerializedName("geometry") val geometry: Geometry,
		@SerializedName("properties") val properties: Properties
	)

	data class Geometry(
		@SerializedName("type") val type: String,
		@SerializedName("coordinates") val coordinates: List<Double>
	)

	data class Properties(
		@SerializedName("@id") val id : String,
		@SerializedName("@type") val type : String,
		@SerializedName("elevation") val elevation: Elevation,
		@SerializedName("stationIdentifier") val stationIdentifier: String,
		@SerializedName("name") val name: String,
		@SerializedName("timeZone") val timeZone: String
	)

	data class Elevation(
		@SerializedName("value") val value: Double,
		@SerializedName("unitCode") val unitCode: String
	)
}