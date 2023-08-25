package kr.kro.tripsketch.utils

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import java.util.Base64

object EncryptionUtils {

    private val secretKeyString = EnvLoader.getProperty("SECRET_KEY") ?: ""

    private fun getAESKey(): SecretKeySpec {
        val key = secretKeyString.toByteArray(Charsets.UTF_8)
        val sha = MessageDigest.getInstance("SHA-256")
        val keyByte = sha.digest(key)
        return SecretKeySpec(keyByte, "AES")
    }

    fun encryptAES(value: String): String {
        val iv = ByteArray(16)
        val secretKeySpec = getAESKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(iv))
        return Base64.getEncoder().encodeToString(cipher.doFinal(value.toByteArray()))
    }

    fun decryptAES(encryptedValue: String): String {
        val iv = ByteArray(16)
        val secretKeySpec = getAESKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(iv))
        return String(cipher.doFinal(Base64.getDecoder().decode(encryptedValue)))
    }
}
