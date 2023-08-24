import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
import kr.kro.tripsketch.utils.EnvLoader

object EncryptionUtils {

    private val secretKeyString = EnvLoader.getProperty("SECRET_KEY") ?: ""

    fun encryptAES(value: String): String {
        val iv = ByteArray(16)
        val secretKeySpec = SecretKeySpec(secretKeyString.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(iv))
        return Base64.getEncoder().encodeToString(cipher.doFinal(value.toByteArray()))
    }

    fun decryptAES(encryptedValue: String): String {
        val iv = ByteArray(16)
        val secretKeySpec = SecretKeySpec(secretKeyString.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(iv))
        return String(cipher.doFinal(Base64.getDecoder().decode(encryptedValue)))
    }
}
