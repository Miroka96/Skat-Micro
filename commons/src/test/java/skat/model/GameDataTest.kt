package skat.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import service.user.RegisterUserData
import kotlin.test.assertEquals


class GameDataTest {

    val jsonMapper = jacksonObjectMapper()

    @Test
    fun gameDataToJson() {
        val data = GameData()
        val dataJson = jsonMapper.writeValueAsString(data)
        val dataRead = jsonMapper.readValue(dataJson, GameData::class.java)
        val dataReadJson = jsonMapper.writeValueAsString(dataRead)
        assertEquals(dataJson, dataReadJson)
    }

    @Test
    fun registerUserDataToJson() {
        val data = RegisterUserData("username")
        data.password = "password"
        data.firstName = "first"
        data.lastName = "last"
        val dataJson = jsonMapper.writeValueAsString(data)
        val dataRead = jsonMapper.readValue(dataJson, RegisterUserData::class.java)
        val dataReadJson = jsonMapper.writeValueAsString(dataRead)
        assertEquals(dataJson, dataReadJson)
    }

    @Test
    fun minimalizedRegisterUserDataToJson() {
        val data = RegisterUserData("username")
        data.password = "password"
        data.firstName = "first"
        data.lastName = "last"
        val dataJson = "{\"username\":\"${data.username}\"}"//jsonMapper.writeValueAsString(data)
        val dataRead = jsonMapper.readValue(dataJson, RegisterUserData::class.java)
        assertEquals(data.username, dataRead.username)
    }

    @Test
    fun dataClassToJson() {
        val data: Data1 = Data1(1, "Foo", "Bar")
        val json = "{\"id\":1,\"first\":\"Foo\",\"last\":\"Bar\"}"

        val json1: String = jsonMapper.writeValueAsString(data)
        val data1: Data1 = jsonMapper.readValue(json1, Data1::class.java)
        val data2: Data2 = jsonMapper.readValue(json1, Data2::class.java)

        val data1_json: String = jsonMapper.writeValueAsString(data1)
        val data2_json: String = jsonMapper.writeValueAsString(data2)

        assertEquals(json, json1)
        assertEquals(json, data1_json)
        assertEquals(json, data2_json)
    }

    data class Data1(val id: Int,
                     val first: String,
                     val last: String)

    data class Data2(@JsonProperty("id") val a: Int,
                     @JsonProperty("first") val b: String,
                     @JsonProperty("last") val c: String)
}