package com.softwaremill.codebrag.licence

import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import org.apache.commons.codec.binary.Base64


object LicenceEncryptor {

  private val SecretString = "qKowZWRHaCGV1cIu"

  private val Key = new SecretKeySpec(getBytesWithCharset(SecretString), "AES")
  private val CipherInstance = Cipher.getInstance("AES/ECB/PKCS5Padding")
  
  def encode(plain: String) = {
    CipherInstance.init(Cipher.ENCRYPT_MODE, Key)
    Base64.encodeBase64String(CipherInstance.doFinal(getBytesWithCharset(plain)))
  }

  def decode(encoded: String) = {
    try {
      CipherInstance.init(Cipher.DECRYPT_MODE, Key)
      new String(CipherInstance.doFinal(Base64.decodeBase64(encoded)))
    } catch {
      case e: Exception => throw new InvalidLicenceKeyException(s"Invalid licence key provided ${encoded}")
    }
  }

  private def getBytesWithCharset(string: String) = string.getBytes(StandardCharsets.UTF_8)

}