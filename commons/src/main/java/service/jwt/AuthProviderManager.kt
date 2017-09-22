package service.jwt

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTOptions
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import service.RoutingPath
import java.util.concurrent.CountDownLatch
import service.ServiceConfigInitializer.Companion as Conf

class AuthProviderManager(val vertx: Vertx, val config: JsonObject) {

    fun readJWTAuthProvider(keyStoreManager: KeyStoreManager, createKeyStore: Boolean): JWTAuth {
        return if (createKeyStore) {
            keyStoreManager.getJWTAuthProvider()
        } else {
            keyStoreManager.getJWTAuthProviderByRead()
        }
    }

    fun pullReadOnlyJWTAuthProvider(): JWTAuth {
        val publicKey = downloadJWTPublicKey()
        println("Got JWT public key:\n$publicKey")
        val jwtConfig = JsonObject()
                .put(KeyStoreManager.PUBLIC_KEY, publicKey)

        val authProvider = JWTAuth.create(vertx, jwtConfig)
        return authProvider
    }

    private fun downloadJWTPublicKey(): String {
        val client = WebClient.create(vertx)
        val request = client.get(
                config.getJsonObject(Conf.SERVICES)
                        .getInteger(Conf.JWT_PORT),
                config.getJsonObject(Conf.SERVICES)
                        .getString(Conf.JWT_HOST),
                RoutingPath.PUBLIC_KEY.toString())

        var publicKey: String = ""
        val lock = CountDownLatch(1)
        request.send { result: AsyncResult<HttpResponse<Buffer>> ->
            if (result.succeeded()) {
                val response = result.result()
                val res = JsonObject(response.bodyAsString())
                publicKey = res.getString(KeyStoreManager.PUBLIC_KEY)
            } else {
                throw result.cause()
            }
            lock.countDown()
        }
        lock.await()
        return publicKey
    }

    fun testTokenCreation(authProvider: JWTAuth, tokenOptions: JWTOptions) {
        println("Testing Token Creation")
        val token: String = authProvider.generateToken(
                JsonObject().put("key", "test"),
                tokenOptions
        )
        print("Sample Token: ")
        println(token)
        println()

        val lock = CountDownLatch(1)
        authProvider.authenticate(
                JsonObject().put("jwt", token)
        ) { res: AsyncResult<User> ->
            print("JWT Test ")
            if (res.succeeded()) {
                println("succeeded")
            } else {
                println("failed")
                throw res.cause()
            }
            lock.countDown()
        }
        lock.await()
    }
}