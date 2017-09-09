package service

enum class WebStatusCode(
        val code: Int
) {
    OK(200),
    CREATED(201),
    BAD(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INTERNAL_ERROR(500);

    fun toInt() = code
}