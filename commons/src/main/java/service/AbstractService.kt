package service

import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import service.database.AbstractQueries
import service.database.CouchbaseAccess
import service.jwt.KeyStoreManager
import service.request.AbstractRequestHandler
import service.request.RequestHandlerWrapper
import java.nio.file.NoSuchFileException
import java.security.UnrecoverableKeyException
import java.util.*


abstract class AbstractService : AbstractVerticle() {
    abstract val serviceName: String
    open val defaultPort = 8080

    val servicesKey = "services"
    val portKey = "port"

    val jwtAlgorithmKey = "jwtAlgorithm"

    val conf: JsonObject by lazy {
        val res = config()

        // complete if not already complete
        // keystore
        val defaultKeystore = JsonObject()
        val keystore = configInsertIfNotExist(res, KeyStoreManager.keystoreKey, defaultKeystore)

        configInsertIfNotExist(keystore, KeyStoreManager.pathKey, "keystore.jceks")
        configInsertIfNotExist(keystore, KeyStoreManager.typeKey, "jceks")
        configInsertIfNotExist(keystore, KeyStoreManager.passwordKey, "secretAsFuq")

        // JWT
        configInsertIfNotExist(res, jwtAlgorithmKey, "RS256")

        //services
        val defaultServices = JsonObject()
        val services = configInsertIfNotExist(res, servicesKey, defaultServices)
        val defaultService = JsonObject()
        val service = configInsertIfNotExist(services, serviceName, defaultService)
        configInsertIfNotExist(service, portKey, defaultPort)

        res
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


    val serviceConfig: JsonObject by lazy {
        conf.getJsonObject(servicesKey).getJsonObject(serviceName)
    }

    protected val router: Router by lazy {
        Router.router(vertx)
    }

    protected lateinit var db: CouchbaseAccess
    abstract val queries: AbstractQueries

    val keyStoreManager by lazy {
        KeyStoreManager(vertx, conf)
    }

    lateinit var authProvider: JWTAuth

    val tokenOptions by lazy {
        JWTOptions().setAlgorithm(conf.getString(jwtAlgorithmKey))
                .setExpiresInMinutes(60)
    }

    final override fun start(future: Future<Void>) {
        val initialized = Future.future<Unit>()
        initialized.setHandler { res: AsyncResult<Unit> ->
            if (res.succeeded()) {
                future.complete()
            } else {
                res.cause().printStackTrace()
                future.fail("shutting service down")
            }
        }
        initialize(initialized)
    }

    private fun initialize(future: Future<Unit>) {
        val authProviderFinished = Future.future<Unit>()
        val databaseFinished = Future.future<Unit>()
        val routingFinished = Future.future<Unit>()
        val customStartFinished = Future.future<Unit>()
        val initializationFinished = Future.future<Unit>()

        authProviderFinished.setHandler { res: AsyncResult<Unit> ->
            if (res.succeeded()) {
                println("Deployed Authentication Provider")
                checkCreateDatabaseIndex(databaseFinished)
            } else {
                future.fail(res.cause())
            }
        }
        databaseFinished.setHandler { res: AsyncResult<Unit> ->
            if (res.succeeded()) {
                println("Deployed Database Connection")
                initializeRouting(routingFinished)
            } else {
                future.fail(res.cause())
            }
        }
        routingFinished.setHandler { res: AsyncResult<Unit> ->
            if (res.succeeded()) {
                println("Deployed Routing Handlers")
                customStart(customStartFinished)
            } else {
                future.fail(res.cause())
            }
        }
        customStartFinished.setHandler { res: AsyncResult<Unit> ->
            if (res.succeeded()) {
                println("Deployed Custom Service Scripts")
                initializeWebServer(initializationFinished)
            } else {
                future.fail(res.cause())
            }
        }
        initializationFinished.setHandler { res: AsyncResult<Unit> ->
            future.handle(res)
        }

        initializeAuthProvider(authProviderFinished)
    }

    protected open val createKeyStorePermission = false

    private fun initializeAuthProvider(continueFuture: Future<Unit>) {
        vertx.executeBlocking<Unit>({ future ->
            try {
                if (createKeyStorePermission) {
                    authProvider = keyStoreManager.getJWTAuthProvider()
                } else {
                    authProvider = keyStoreManager.getJWTAuthProviderByRead()
                }
                checkAuthProvider(future)
            } catch (keyEx: UnrecoverableKeyException) {
                future.fail(keyEx)
            } catch (nsfEx: NoSuchFileException) {
                future.fail(nsfEx)
            } catch (ex: Exception) {
                println("Unknown Exception while reading Keystore")
                future.fail(ex)
            }
        }, { res: AsyncResult<Unit> ->
            continueFuture.handle(res)
        })
    }

    private fun checkAuthProvider(continueFuture: Future<Unit>) {
        val token: String = authProvider.generateToken(
                JsonObject().put("key", "test")
                , tokenOptions
        )
        print("Sample Token: ")
        println(token)

        try {
            val keyStore = keyStoreManager.loadKeyStore()
            val publicKey = keyStoreManager.getPublicKey(keyStore, tokenOptions.algorithm)

            print("Public Key: ")
            println(Base64.getEncoder().encodeToString(publicKey.encoded))
        } catch (ex: NullPointerException) {
            println("No public key found for algorithm '${tokenOptions.algorithm}'")
        }


        authProvider.authenticate(
                JsonObject().put("jwt", token)
        ) { res: AsyncResult<User> ->
            if (res.succeeded()) {
                continueFuture.complete()
            } else {
                println("JWT Test failed")
                continueFuture.fail(res.cause())
            }
        }
    }

    private fun checkCreateDatabaseIndex(continueFuture: Future<Unit>) {
        vertx.executeBlocking<Unit>({ future: Future<Unit> ->
            db = CouchbaseAccess(config())
            db.checkBucket()
                    .flatMap { bucket ->
                        bucket.bucketManager()
                    }
                    .flatMap { mgr ->
                        mgr.createN1qlPrimaryIndex(true, true)
                        mgr.buildN1qlDeferredIndexes()
                    }
                    .toBlocking()
                    .last()
            future.complete()
        }, { res: AsyncResult<Unit> ->
            continueFuture.complete()
        })
    }

    private fun initializeRouting(continueFuture: Future<Unit>) {
        router.route().handler(BodyHandler.create()) //This is really important if you use routing
        // -> read documentation. If not used the body wont be passed

        addRouting(router)
        continueFuture.complete()
    }

    private fun initializeWebServer(continueFuture: Future<Unit>) {
        vertx
                .createHttpServer()
                .requestHandler(router::accept)

                .listen(
                        // Retrieve the port from the configuration,
                        // default to 8080
                        config().getInteger("http.port", defaultPort)
                ) { result ->
                    if (result.succeeded()) {
                        continueFuture.complete()
                    } else {
                        continueFuture.fail(result.cause())
                    }
                }
    }

    final override fun stop() {
        db.closeBlocking(true)
        customStop()
    }

    protected abstract fun addRouting(router: Router)

    protected open fun customStart(continueFuture: Future<Unit>) {
        continueFuture.complete()
    }

    protected open fun customStop() {}

    protected fun wrapHandler(requestHandler: AbstractRequestHandler): Handler<RoutingContext>
            = RequestHandlerWrapper(requestHandler, db, this).wrapHandler()
}