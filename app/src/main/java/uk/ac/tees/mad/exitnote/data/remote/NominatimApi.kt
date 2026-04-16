package uk.ac.tees.mad.exitnote.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

// --------------------
// RESPONSE MODELS
// --------------------

data class NominatimResponse(
    @SerializedName("display_name")
    val displayName: String?,
    @SerializedName("address")
    val address: Address?
)

data class Address(
    @SerializedName("city")
    val city: String?,
    @SerializedName("town")
    val town: String?,
    @SerializedName("village")
    val village: String?,
    @SerializedName("state")
    val state: String?,
    @SerializedName("country")
    val country: String?
)

// --------------------
// API INTERFACE
// --------------------

interface NominatimApi {

    @Headers(
        "User-Agent: ExitNote-Android/1.0 (student@student.tees.ac.uk)",
        "Referer: https://uk.ac.tees.mad.exitnote"
    )
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1
    ): NominatimResponse

    companion object {

        private const val BASE_URL = "https://nominatim.openstreetmap.org/"

        fun create(): NominatimApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NominatimApi::class.java)
        }
    }
}
