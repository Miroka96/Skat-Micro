package jwt

import io.vertx.core.Vertx
import io.vertx.core.file.FileSystemException
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import java.io.FileOutputStream
import java.io.IOException
import java.lang.RuntimeException
import java.nio.file.NoSuchFileException
import java.security.KeyStore
import java.security.UnrecoverableKeyException

class KeyStoreManager(val vertx: Vertx, val config: JsonObject) {
    val keystoreKey = "keyStore"
    val defaultPath = "keystore.jceks"
    val defaultType = "jceks"
    val defaultPassword = "secretAsFuq"

    fun getJWTAuthProvider(): JWTAuth {
        checkConfig()
        try {
            return readKeyStore()
        } catch (nsfEx: NoSuchFileException) {
            val params = config.getJsonObject(keystoreKey)

            val type = params.getString("type", defaultType)
            val password = params.getString("password", defaultPassword)
            val path = params.getString("path", defaultPath)

            createKeyStore(type, password, path)
            initializeKeyStore(type, password, path)
            return readKeyStore()
        }
    }

    private fun checkConfig() {
        if (!config.containsKey(keystoreKey)) {
            val defaultKeystore = JsonObject()
                    .put("path", defaultPath)
                    .put("type", defaultType)
                    .put("password", defaultPassword)
            config.put("keyStore", defaultKeystore)
        }
    }

    private fun readKeyStore(): JWTAuth {
        try {
            return JWTAuth.create(vertx, config)
        } catch (ex: RuntimeException) {
            if (ex.cause is IOException) {
                val ioEx = ex.cause!!
                if (ioEx.cause is UnrecoverableKeyException) {
                    val keyEx = ioEx.cause!!
                    println("Tried accessing Keystore with wrong Password")
                    throw keyEx
                }
            } else if (ex.cause is FileSystemException) {
                val fsEx = ex.cause!!
                if (fsEx.cause is NoSuchFileException) {
                    val nsfEx = fsEx.cause!!
                    println("Did not find Keystore File: ${nsfEx.message}")
                    throw nsfEx
                }
            }
            println("Unknown Exception occured while creating JWT Authenticator")
            ex.printStackTrace()
            throw ex
        }
    }

    private fun createKeyStore(type: String = defaultType, password: String = defaultPassword, path: String = defaultPath) {
        val ks = KeyStore.getInstance(type)
        ks.load(null, password.toCharArray())

        // Store away the keystore.
        val fos = FileOutputStream(path)
        ks.store(fos, password.toCharArray())
        fos.close()
    }

    private fun initializeKeyStore(type: String = defaultType, password: String = defaultPassword, path: String = defaultPath) {
        val keytool = KeyTool()
        keytool.initialize(path, type, password)
    }
}