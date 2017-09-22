package service

import io.vertx.core.*
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import service.database.AbstractQueries
import service.database.CouchbaseAccess
import service.jwt.AuthProviderManager
import service.jwt.KeyStoreManager
import service.request.AbstractRequestHandler
import service.request.RequestHandlerWrapper
import java.nio.file.NoSuchFileException
import java.security.UnrecoverableKeyException


abstract class AbstractService : AbstractVerticle() {

    abstract val serviceName: String
    open val defaultPort = 8080

    val defaultJWTPort = 8081
    val defaultJWTHost = "localhost"

    val configInitializer by lazy {
        ServiceConfigInitializer(config(), this)
    }

    val conf: JsonObject by lazy {
        configInitializer
                .initializeKeystore()
                .initializeJwt()
                .initializeService()
                .config
    }


    val serviceConfig: JsonObject by lazy {
        configInitializer.getServiceConfig()
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
        JWTOptions().setAlgorithm(conf.getString(ServiceConfigInitializer.JWT_ALGORITHM))
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

        val step1Futures = listOf(
                authProviderFinished,
                databaseFinished
        )
        val preparationFinished = CompositeFuture.all(step1Futures)


        val customStartFinished = Future.future<Unit>()
        val routingFinished = Future.future<Unit>()

        val step2Futures = listOf(
                customStartFinished,
                routingFinished
        )
        val initializationFinished = CompositeFuture.all(step2Futures)


        val webserverFinished = Future.future<Unit>()


        preparationFinished.setHandler { res ->
            if (res.succeeded()) {
                println("Finished Initialization Step 1")
                customStart(customStartFinished)
                initializeRouting(routingFinished)
            } else {
                future.fail(res.cause())
            }
        }

        initializationFinished.setHandler { res ->
            if (res.succeeded()) {
                println("Finished Initialization Step 2")
                initializeWebServer(webserverFinished)
            } else {
                future.fail(res.cause())
            }
        }

        webserverFinished.setHandler { res ->
            println("Finished Initialization Step 3")
            future.handle(res)
        }

        initializeAuthProvider(authProviderFinished)
        checkCreateDatabaseIndex(databaseFinished)
    }

    protected open val createKeyStorePermission = false

    private fun initializeAuthProvider(continueFuture: Future<Unit>) {
        vertx.executeBlocking<Unit>({ future ->
            val authProviderManager = AuthProviderManager(vertx, conf)
            try {
                authProvider = authProviderManager.readJWTAuthProvider(keyStoreManager, createKeyStorePermission)
                authProviderManager.testTokenCreation(authProvider, tokenOptions)
            } catch (keyEx: UnrecoverableKeyException) {
                future.fail(keyEx)
            } catch (nsfEx: NoSuchFileException) {
                println("Pulling Public Key")
                try {
                    authProvider = authProviderManager.pullReadOnlyJWTAuthProvider()
                } catch (ex: Exception) {
                    println("Exception while pulling public key")
                    ex.printStackTrace()
                    future.fail(ex)
                }
            } catch (ex: Exception) {
                println("Unknown Exception while reading Keystore")
                future.fail(ex)
            }
            future.complete()
        }, { res: AsyncResult<Unit> ->
            continueFuture.handle(res)
        })
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