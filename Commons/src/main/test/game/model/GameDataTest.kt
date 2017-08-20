package game.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.junit.Arquillian
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(Arquillian::class)
class GameDataTest {

    val jsonMapper = jacksonObjectMapper()

    @Test
    fun dataClassToJson() {
        val data: Data1 = Data1(1, "Foo", "Bar")
        val json = "{\"id\":1,\"first\":\"Foo\",\"last\":\"Bar\"}"

        val json1: String = jsonMapper.writeValueAsString(data)
        val data1: Data1 = jsonMapper.readValue(json1, Data1::class.java)
        val data2: Data2 = jsonMapper.readValue(json1, Data2::class.java)

        val data1_json: String = jsonMapper.writeValueAsString(data1)
        val data2_json: String = jsonMapper.writeValueAsString(data2)

        /*
        println(data)
        println(json1)
        println(data1)
        println(data2)
        println(data1_json)
        println(data2_json)
        */

        assertEquals(json, json1)
        assertEquals(json, data1_json)
        assertEquals(json, data2_json)
    }

    companion object {

        @Deployment
        fun createDeployment(): JavaArchive {
            return ShrinkWrap.create(JavaArchive::class.java)
                    .addClass(GameData::class.java)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
        }
    }

    data class Data1(val id: Int,
                     val first: String,
                     val last: String)

    data class Data2(@JsonProperty("id") val a: Int,
                     @JsonProperty("first") val b: String,
                     @JsonProperty("last") val c: String)
}