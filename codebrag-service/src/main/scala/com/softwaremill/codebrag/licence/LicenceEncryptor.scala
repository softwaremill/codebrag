package com.softwaremill.codebrag.licence

import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import org.apache.commons.codec.binary.Base64

object LicenceEncryptor {

  val SecretString = "qKowZWRHaCGV1cIu"

  val Key = new SecretKeySpec(SecretString.getBytes(StandardCharsets.UTF_8), "AES")
  val CipherInstance = Cipher.getInstance("AES/ECB/PKCS5Padding")

  def encode(licence: LicenceDetails): String = {
    CipherInstance.init(Cipher.ENCRYPT_MODE, Key)
    Base64.encodeBase64String(CipherInstance.doFinal(licence.toJson.getBytes))
  }

  def decode(licenceKey: String): LicenceDetails = {
    CipherInstance.init(Cipher.DECRYPT_MODE, Key)
    LicenceDetails(new String(CipherInstance.doFinal(Base64.decodeBase64(licenceKey))))
  }

}
