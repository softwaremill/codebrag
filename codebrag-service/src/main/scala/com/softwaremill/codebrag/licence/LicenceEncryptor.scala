package com.softwaremill.codebrag.licence

import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import org.apache.commons.codec.binary.Base64

object LicenceEncryptor {

  private val SecretString = "qKowZWRHaCGV1cIu"

  private val Key = new SecretKeySpec(SecretString.getBytes(StandardCharsets.UTF_8), "AES")
  private val CipherInstance = Cipher.getInstance("AES/ECB/PKCS5Padding")

  def encode(licence: Licence): String = {
    CipherInstance.init(Cipher.ENCRYPT_MODE, Key)
    Base64.encodeBase64String(CipherInstance.doFinal(licence.toJsonString.getBytes))
  }

  def decode(licenceKey: String): Licence = {
    try {
      CipherInstance.init(Cipher.DECRYPT_MODE, Key)
      Licence(new String(CipherInstance.doFinal(Base64.decodeBase64(licenceKey))))
    } catch {
      case e: Exception => throw new InvalidLicenceKeyException(s"Invalid licence key provided ${licenceKey}")
    }
  }

}