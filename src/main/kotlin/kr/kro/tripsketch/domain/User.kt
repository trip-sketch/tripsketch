import java.util.Date

class User(
    val nickName: String,
    val userName: String,
    val password: String,
    val role: String,
    val phoneNumber: String,
    val email: String,
    val intro: String
) {
    val id: Long? = null
    val createdAt: Date = Date()
    val updatedAt: Date = Date()
    val deletedAt: Date? = null
}