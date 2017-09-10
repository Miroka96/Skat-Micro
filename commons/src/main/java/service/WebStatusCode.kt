package service

enum class WebStatusCode(
        val code: Int
) {
    OK(200),
    CREATED(201),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INTERNAL_ERROR(500),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIME_OUT(504);

    fun toInt() = code
}