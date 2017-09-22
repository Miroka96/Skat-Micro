package service

import io.vertx.core.json.JsonObject
import service.jwt.KeyStoreManager

class ServiceConfigInitializer(val config: JsonObject,
                               val service: AbstractService
) {

    fun initializeKeystore(): ServiceConfigInitializer {
        val defaultKeystore = JsonObject()
        val keystore = configInsertIfNotExist(config, KeyStoreManager.KEYSTORE, defaultKeystore)

        configInsertIfNotExist(keystore, KeyStoreManager.PATH, "keystore.jceks")
        configInsertIfNotExist(keystore, KeyStoreManager.TYPE, "jceks")
        configInsertIfNotExist(keystore, KeyStoreManager.PASSWORD, "secretAsFuq")
        return this
    }

    fun initializeJwt(): ServiceConfigInitializer {
        configInsertIfNotExist(config, JWT_ALGORITHM, "RS256")
        return this
    }

    fun initializeService(): ServiceConfigInitializer {
        val defaultServices = JsonObject()
        val services = configInsertIfNotExist(config, SERVICES, defaultServices)
        val defaultService = JsonObject()
        val serviceConfig = configInsertIfNotExist(services, service.serviceName, defaultService)

        configInsertIfNotExist(serviceConfig, PORT, service.defaultPort)

        configInsertIfNotExist(services, JWT_HOST, service.defaultJWTHost)
        configInsertIfNotExist(services, JWT_PORT, service.defaultJWTPort)

        println(config.encodePrettily())
        println()
        return this
    }

    fun configInsertIfNotExist(config: JsonObject, key: String, value: String): String {
        if (!config.containsKey(key)) {
            config.put(key, value)
            return value
        }
        return config.getString(key)
    }

    fun configInsertIfNotExist(config: JsonObject, key: String, value: JsonObject): JsonObject {
        if (!config.containsKey(key)) {
            config.put(key, value)
            return value
        }
        return config.getJsonObject(key)
    }

    fun configInsertIfNotExist(config: JsonObject, key: String, value: Int): Int {
        if (!config.containsKey(key)) {
            config.put(key, value)
            return value
        }
        return config.getInteger(key)
    }

    fun getServiceConfig(): JsonObject {
        return config
                .getJsonObject(SERVICES)
                .getJsonObject(service.serviceName)
    }

    companion object {
        val SERVICES = "services"
        val PORT = "port"
        val JWT_HOST = "jwtHost"
        val JWT_PORT = "jwtPort"
        val JWT_ALGORITHM = "jwtAlgorithm"
    }
}