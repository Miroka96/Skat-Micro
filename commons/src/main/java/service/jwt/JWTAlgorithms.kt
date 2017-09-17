package service.jwt

enum class JWTAlgorithms(
        val alias: String,
        val keyAlg: String,
        val sigAlg: String = "",
        val keySize: Int = 2048,
        val type: String = alias.substring(0, 2)
) {
    // symmetric keys
    HS256(
            "HS256",
            "HMacSHA256"
    ),
    HS384(
            "HS384",
            "HMacSHA384"
    ),
    HS512(
            "HS512",
            "HMacSHA512"
    ),

    // fast verification, but larger asymmetric keys
    RS256(
            "RS256",
            "RSA",
            "SHA256withRSA"
    ),
    RS384(
            "RS384",
            "RSA",
            "SHA384withRSA"
    ),
    RS512(
            "RS512",
            "RSA",
            "SHA512withRSA"
    ),

    // small asymmetric keys, but slower verification
    ES256(
            "ES256",
            "EC",
            "SHA256withECDSA",
            256
    ),
    ES384(
            "ES384",
            "EC",
            "SHA384withECDSA",
            256
    ),
    ES512(
            "ES512",
            "EC",
            "SHA512withECDSA",
            256
    );

    companion object {
        fun getAlgorithmByAlias(alias: String): JWTAlgorithms {
            for (alg in values()) {
                if (alg.alias.equals(alias)) {
                    return alg
                }
            }
            throw IllegalArgumentException("JWT Algorithm Alias '$alias' does not exist")
        }
    }
}