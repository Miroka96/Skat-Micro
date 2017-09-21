import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.unit.TestContext
import org.junit.Test

class AuthenticationTokenTest : AbstractVertxTest() {

    @Test
    fun testToken(context: TestContext) {
        val pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjly4zaxP5V5R6qM3Ky8w0D2PHPkVjNAWGe1rZmFSLFDs/75CfvHOB+O2esBBYj/p4h+Vtt7fJk8JtsKq4T3q0vMRtoJxo6Vae++QEKu273QsLyYkumiQjMRr4/iEc6j0WG6a9b8FJ24sOWyHKjPSBNebLWRhszmE3l9b34sza2nywjk94B41n7rFA2eH6hah1Rm6chBzaPWAjOA9rSfPWoSOJYrfkUPebnx8JdDKC1E1esvr7JR5XgtU3q5iYqjIXmbtwuUE/a3PuMb82ca8J1u1h3/lRsI5tQt0AG1QcjvYwnhME3/Dupjz8CclIOze9zljFdvEMmprubfrGLh3PwIDAQAB"
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJrZXkiOiJ0ZXN0IiwiaWF0IjoxNTA2MDEyMDMzLCJleHAiOjE1MDYwMTU2MzN9.Sv_YPatMHlAbtd5lzY73MP7xPcyZ0FzKp-wrt3IlYc0rwFVFsRybbZJluLUE8qGD4Sq616WdLlDJPgX6vLYVN8_1rIme999nWzU7UqnSkBgwWDDJVZUYWwcSj4beDkFqizyi9gOMXRpElaWraNvwSo1JZ8yEIbozSZyBLyDNNfdVbkBencac6blaSf3z65gOrzVa6yJAQA-Q3dDegNnRFR1aCpRjq65DAKXIpLjQcTJkqPgHax88JCpZnoHSvXLoYeBHVBCzwzaOKA47Q4isQOUrrCWDCxuwoQR1ViXUSM2w6q2q2Cqsyj0sZHkFJmJytiRpIMUc7FBGkpoZv89OoA"

        val authConf = JsonObject()
                .put("public-key", pubKey)
        val authProvider = JWTAuth.create(vertx, authConf)
        authProvider.authenticate(
                JsonObject().put("jwt", token),
                context.asyncAssertSuccess()
        )
    }

    @Test
    fun testInvalidToken(context: TestContext) {
        val pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjly4zaxP5V5R6qM3Ky8w0D2PHPkVjNAWGe1rZmFSLFDs/75CfvHOB+O2esBBYj/p4h+Vtt7fJk8JtsKq4T3q0vMRtoJxo6Vae++QEKu273QsLyYkumiQjMRr4/iEc6j0WG6a9b8FJ24sOWyHKjPSBNebLWRhszmE3l9b34sza2nywjk94B41n7rFA2eH6hah1Rm6chBzaPWAjOA9rSfPWoSOJYrfkUPebnx8JdDKC1E1esvr7JR5XgtU3q5iYqjIXmbtwuUE/a3PuMb82ca8J1u1h3/lRsI5tQt0AG1QcjvYwnhME3/Dupjz8CclIOze9zljFdvEMmprubfrGLh3PwIDAQAB"
        val token = "9.eyJrZXkiOiJ0ZXN0IiwiaWF0IjoxNTA2MDEyMDMzLCJleHAiOjE1MDYwMTU2MzN9.Sv_YPatMHlAbtd5lzY73MP7xPcyZ0FzKp-wrt3IlYc0rwFVFsRybbZJluLUE8qGD4Sq616WdLlDJPgX6vLYVN8_1rIme999nWzU7UqnSkBgwWDDJVZUYWwcSj4beDkFqizyi9gOMXRpElaWraNvwSo1JZ8yEIbozSZyBLyDNNfdVbkBencac6blaSf3z65gOrzVa6yJAQA-Q3dDegNnRFR1aCpRjq65DAKXIpLjQcTJkqPgHax88JCpZnoHSvXLoYeBHVBCzwzaOKA47Q4isQOUrrCWDCxuwoQR1ViXUSM2w6q2q2Cqsyj0sZHkFJmJytiRpIMUc7FBGkpoZv89OoA"

        val authConf = JsonObject()
                .put("public-key", pubKey)
        val authProvider = JWTAuth.create(vertx, authConf)
        authProvider.authenticate(
                JsonObject().put("jwt", token),
                context.asyncAssertFailure()
        )
    }
}