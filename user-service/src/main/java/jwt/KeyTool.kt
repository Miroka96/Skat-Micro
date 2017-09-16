package jwt

class KeyTool {

    fun initialize(
            keyStore: String = "keystore.jceks",
            storeType: String = "jceks",
            password: String = "secret",
            dname: String = "CN=,OU=,O=,L=,ST=,C=",
            validity: Int = 360
    ) {
        generateSecKey(
                keyStore,
                storeType,
                password,
                "HMacSHA256",
                2048,
                "HS256",
                password
        )
        generateSecKey(
                keyStore,
                storeType,
                password,
                "HMacSHA384",
                2048,
                "HS384",
                password
        )
        generateSecKey(
                keyStore,
                storeType,
                password,
                "HMacSHA512",
                2048,
                "HS512",
                password
        )

        generateKey(
                keyStore,
                storeType,
                password,
                "RSA",
                2048,
                "RS256",
                password,
                "SHA256withRSA",
                dname,
                validity
        )
        generateKey(
                keyStore,
                storeType,
                password,
                "RSA",
                2048,
                "RS384",
                password,
                "SHA384withRSA",
                dname,
                validity
        )
        generateKey(
                keyStore,
                storeType,
                password,
                "RSA",
                2048,
                "RS512",
                password,
                "SHA512withRSA",
                dname,
                validity
        )

        generateKeyPair(
                keyStore,
                storeType,
                password,
                "EC",
                256,
                "ES256",
                password,
                "SHA256withECDSA",
                dname,
                validity
        )
        generateKeyPair(
                keyStore,
                storeType,
                password,
                "EC",
                256,
                "ES384",
                password,
                "SHA384withECDSA",
                dname,
                validity
        )
        generateKeyPair(
                keyStore,
                storeType,
                password,
                "EC",
                256,
                "ES512",
                password,
                "SHA512withECDSA",
                dname,
                validity
        )
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