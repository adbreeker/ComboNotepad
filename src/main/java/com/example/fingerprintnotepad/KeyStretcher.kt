package com.example.fingerprintnotepad

import android.content.Context
import java.math.BigInteger
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class KeyStretcher
{
        @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
        fun stretch(original_password: String, saltspecified: ByteArray) : String
        {
            val originalPassword = original_password
            val generatedSecuredPasswordHash = generateStrongPasswordHash(originalPassword, saltspecified)
            return generatedSecuredPasswordHash
        }

        @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
        private fun generateStrongPasswordHash(password: String, saltspecified: ByteArray): String
        {
            val iterations = 500
            val chars = password.toCharArray()
            val salt = saltspecified
            val spec = PBEKeySpec(chars, salt, iterations, 64 * 8)
            val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val hash = skf.generateSecret(spec).encoded
            return toHex(hash)
        }


        fun salt(): String
        {
            val sr: SecureRandom = SecureRandom.getInstance("SHA1PRNG")
            val salt = ByteArray(16)
            sr.nextBytes(salt)
            return salt.decodeToString()
        }

        @Throws(NoSuchAlgorithmException::class)
        private fun toHex(array: ByteArray): String
        {
            val bi = BigInteger(1, array)
            val hex = bi.toString(16)
            val paddingLength = array.size * 2 - hex.length
            return if (paddingLength > 0)
            {
                String.format("%0" + paddingLength + "d", 0) + hex
            }
            else
            {
                hex
            }
        }

}