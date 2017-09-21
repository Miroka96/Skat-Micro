package service.jwt

import io.vertx.core.Vertx
import io.vertx.core.file.FileSystemException
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.RuntimeException
import java.nio.file.NoSuchFileException
import java.security.KeyStore
import java.security.PublicKey
import java.security.UnrecoverableKeyException

class KeyStoreManager(
        val vertx: Vertx,
        val config: JsonObject, // root config needed
        val defaultPath: String = config.getJsonObject(KEYSTORE).getString(PATH),
        val defaultType: String = config.getJsonObject(KEYSTORE).getString(TYPE),
        val defaultPassword: String = config.getJsonObject(KEYSTORE).getString(PASSWORD)
) {
    fun getJWTAuthProvider(): JWTAuth {
        try {
            return getJWTAuthProviderByRead()
        } catch (nsfEx: NoSuchFileException) {
            val params = config.getJsonObject(KEYSTORE)

            val type = params.getString(TYPE, defaultType)
            val password = params.getString(PASSWORD, defaultPassword)
            val path = params.getString(PATH, defaultPath)

            createKeyStore(type, password, path)
            initializeKeyStore(type, password, path)
            return readKeyStore()
        }
    }

    fun getJWTAuthProviderByRead(): JWTAuth {
        return readKeyStore()
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
        println("Created Keystore File: $path")
    }


    fun loadKeyStore(type: String = defaultType, password: String = defaultPassword, path: String = defaultPath): KeyStore {
        val ks = KeyStore.getInstance(type)
        val fis = FileInputStream(path)
        ks.load(fis, password.toCharArray())
        fis.close()
        return ks
    }

    // throws NullPointerException if 'alias' is invalid
    fun getPublicKey(keyStore: KeyStore, keyAlias: String): PublicKey {
        val cert = keyStore.getCertificate(keyAlias)
        return cert.publicKey
    }

    private fun initializeKeyStore(type: String = defaultType, password: String = defaultPassword, path: String = defaultPath) {
        val keytool = KeyTool()
        keytool.initializeAll(path, type, password)
    }

    companion object {
        val KEYSTORE = "keyStore"
        val TYPE = "type"
        val PASSWORD = "password"
        val PATH = "path"

        val PUBLIC_KEY = "public-key"
        val JWT = "jwt"
    }
}