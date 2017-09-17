package service.jwt

class KeyTool {

    fun initializeAll(
            keyStore: String = "keystore.jceks",
            storeType: String = "jceks",
            password: String,
            dname: String = "CN=,OU=,O=,L=,ST=,C=",
            validity: Int = 360
    ) {
        for (algorithm in JWTAlgorithms.values()) {
            initialize(
                    keyStore,
                    storeType,
                    password,
                    dname,
                    validity,
                    algorithm
            )
        }
    }

    fun initialize(
            keyStore: String = "keystore.jceks",
            storeType: String = "jceks",
            password: String,
            dname: String = "CN=,OU=,O=,L=,ST=,C=",
            validity: Int = 360,
            algorithm: JWTAlgorithms
    ) {
        when (algorithm.type) {
            "HS" -> generateSecKey(
                    keyStore,
                    storeType,
                    password,
                    algorithm.keyAlg,
                    algorithm.keySize,
                    algorithm.alias,
                    password
            )
            "RS" -> generateKey(
                    keyStore,
                    storeType,
                    password,
                    algorithm.keyAlg,
                    algorithm.keySize,
                    algorithm.alias,
                    password,
                    algorithm.sigAlg,
                    dname,
                    validity
            )
            "ES" -> generateKeyPair(
                    keyStore,
                    storeType,
                    password,
                    algorithm.keyAlg,
                    algorithm.keySize,
                    algorithm.alias,
                    password,
                    algorithm.sigAlg,
                    dname,
                    validity
            )
        }
    }

    fun generateSecKey(
            keystore: String = "keystore.jceks",
            storeType: String = "jceks",
            storePass: String = "secret",
            keyAlg: String = "HMacSHA256",
            keySize: Int = 2048,
            alias: String = "HS256",
            keyPass: String = storePass
    ) {
        val command = "-genseckey -keystore $keystore -storetype $storeType -storepass $storePass -keyalg $keyAlg -keysize $keySize -alias $alias -keypass $keyPass"
        execute(command)
    }

    fun generateKey(
            keyStore: String = "keystore.jceks",
            storeType: String = "jceks",
            storePass: String = "secret",
            keyAlg: String = "HMacSHA256",
            keySize: Int = 2048,
            alias: String = "HS256",
            keyPass: String = storePass,
            sigAlg: String = "SHA256withRSA",
            dname: String = "CN=,OU=,O=,L=,ST=,C=",
            validity: Int = 360
    ) {
        val command = "-genkey -keystore $keyStore -storetype $storeType -storepass $storePass -keyalg $keyAlg -keysize $keySize -alias $alias -keypass $keyPass -sigalg $sigAlg -dname $dname -validity $validity"
        execute(command)
    }

    fun generateKeyPair(
            keyStore: String = "keystore.jceks",
            storeType: String = "jceks",
            storePass: String = "secret",
            keyAlg: String = "EC",
            keySize: Int = 256,
            alias: String = "ES256",
            keyPass: String = storePass,
            sigAlg: String = "SHA256withECDSA",
            dname: String = "CN=,OU=,O=,L=,ST=,C=",
            validity: Int = 360
    ) {
        val command = "-genkeypair -keystore $keyStore -storetype $storeType -storepass $storePass -keyalg $keyAlg -keysize $keySize -alias $alias -keypass $keyPass -sigalg $sigAlg -dname $dname -validity $validity"
        execute(command)
    }

    fun execute(command: String) {
        sun.security.tools.keytool.Main.main(parse(command))
    }

    // Parse command
    private fun parse(command: String): Array<String>
            = command.trim { it <= ' ' }.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

}