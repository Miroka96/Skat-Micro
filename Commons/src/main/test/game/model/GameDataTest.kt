package game.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.junit.Arquillian
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.junit.runner.RunWith

@RunWith(Arquillian::class)
class GameDataTest {

    @org.junit.Test
    fun toJson() {
        //var gameData = GameData()

        val mapper = jacksonObjectMapper()
        val writer = mapper.writerWithDefaultPrettyPrinter()

        val json1 = writer.writeValueAsString(Data1(1, "Foo", "Bar"))
        val data1_1 = mapper.readValue(json1, Data1::class.java)
        val data2_1 = mapper.readValue(json1, Data2::class.java)
        println(json1)
        println()
        println(data1_1)
        println(data2_1)
        println()
        val json2 = writer.writeValueAsString(Data2(1, "Baz", "Qux"))
        val data1_2 = mapper.readValue(json2, Data1::class.java)
        val data2_2 = mapper.readValue(json2, Data2::class.java)
        println(json2)
        println()
        println(data1_2)
        println(data2_2)
    }

    companion object {

        @Deployment
        fun createDeployment(): JavaArchive {
            return ShrinkWrap.create(JavaArchive::class.java)
                    .addClass(GameData::class.java)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
        }
    }

}

data class Data1(val id: Int,
                 val first: String,
                 val last: String)

data class Data2(@JsonProperty("id") val a: Int,
                 @JsonProperty("first") val b: String,
                 @JsonProperty("last") val c: String)
