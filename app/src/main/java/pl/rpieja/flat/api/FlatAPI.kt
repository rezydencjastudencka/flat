package pl.rpieja.flat.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonIOException
import okhttp3.*
import pl.rpieja.flat.dto.*
import java.util.*

class FlatAPI(cookieJar: CookieJar) {

    private val client: OkHttpClient = OkHttpClient.Builder().cookieJar(cookieJar).build()
    private val gson = Gson()

    fun login(username: String, password: String): Boolean? {
        //TODO: use Gson
        val json = "{\"name\":\"$username\", \"password\": \"$password\"}"

        val request = Request.Builder()
                .url(CREATE_SESSION_URL)
                .post(RequestBody.create(JSON_MEDIA_TYPE, json))
                .build()
        val response = client.newCall(request).execute()
        return response.isSuccessful
    }

    fun validateSession(): Boolean? {
        val request = Request.Builder()
                .url(SESSION_CHECK_URL)
                .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return false
        val (error) = gson.fromJson(response.body()!!.string(), SessionCheckResponse::class.java)
                ?: return false
        return "ok" == error
    }

    fun fetchCharges(month: Int, year: Int): ChargesDTO {
        val requestUrl = GET_CHARGES_URL + Integer.toString(year) + "/" + Integer.toString(month)
        return fetch(requestUrl, ChargesDTO::class.java)
    }

    fun fetchTransfers(month: Int, year: Int): TransfersDTO {
        val requestUrl = GET_TRANSFERS_URL + Integer.toString(year) + "/" + Integer.toString(month)
        return fetch(requestUrl, TransfersDTO::class.java)
    }

    fun fetchUsers(): List<User> {
        return Arrays.asList(*fetch(GET_USERS_URL, Array<User>::class.java))
    }

    private fun <T> createEntity(entity: CreateDTO<T>, entityUrl: String): T {
        val response = post(entityUrl, entity)
        return gson.fromJson(response.body()!!.charStream(), entity.entityClass)
    }

    fun createCharge(charge: CreateChargeDTO): Charge {
        return createEntity(charge, CREATE_CHARGE_URL)
    }

    private fun <T> post(url: String, data: T): Response {
        return method("POST", url, data)
    }

    private fun <T> put(url: String, data: T) {
        method("PUT", url, data)
    }

    private fun <T> method(methodName: String, url: String, data: T): Response {
        val json = gson.toJson(data)
        val request = Request.Builder()
                .url(url)
                .method(methodName, RequestBody.create(JSON_MEDIA_TYPE, json))
                .build()

        Log.d(TAG, String.format("Sending %s %s with data %s", methodName, url, json))
        val response = client.newCall(request).execute()
        if (response.code() == 403) throw UnauthorizedException()
        if (!response.isSuccessful) throw NoInternetConnectionException()
        return response
    }

    private fun <T> fetch(requestUrl: String, tClass: Class<T>): T {
        val request = Request.Builder()
                .url(requestUrl)
                .build()

        val response = client.newCall(request).execute()
        if (response.code() == 403) throw UnauthorizedException()
        if (!response.isSuccessful) throw NoInternetConnectionException()

        return gson.fromJson(response.body()!!.string(), tClass)
                ?: throw JsonIOException("JSON parsing exception")
    }

    companion object {

        private const val API_ADDRESS = "https://api.flat.memleak.pl/"
        private val JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8")
        private const val SESSION_CHECK_URL = API_ADDRESS + "session/check"
        private const val CREATE_SESSION_URL = API_ADDRESS + "session/create"
        private const val GET_CHARGES_URL = API_ADDRESS + "charge/"
        private const val GET_TRANSFERS_URL = API_ADDRESS + "transfer/"
        private const val CREATE_CHARGE_URL = API_ADDRESS + "charge/create"
        private const val GET_USERS_URL = API_ADDRESS + "user/"

        private val TAG = FlatAPI::class.java.simpleName
    }

}