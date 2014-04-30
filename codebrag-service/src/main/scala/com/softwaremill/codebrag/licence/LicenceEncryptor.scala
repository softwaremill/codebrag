package com.softwaremill.codebrag.licence

import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import org.apache.commons.codec.binary.Base64
import org.joda.time.DateTime

object LicenceEncryptor {

  private val SecretString = "qKowZWRHaCGV1cIu"

  private val Key = new SecretKeySpec(SecretString.getBytes(StandardCharsets.UTF_8), "AES")
  private val CipherInstance = Cipher.getInstance("AES/ECB/PKCS5Padding")

  def encode(licence: LicenceDetails): String = {
    CipherInstance.init(Cipher.ENCRYPT_MODE, Key)
    Base64.encodeBase64String(CipherInstance.doFinal(licence.toJson.getBytes))
  }

  def decode(licenceKey: String): LicenceDetails = {
    try {
      CipherInstance.init(Cipher.DECRYPT_MODE, Key)
      LicenceDetails(new String(CipherInstance.doFinal(Base64.decodeBase64(licenceKey))))
    } catch {
      case e: Exception => throw new InvalidLicenceKeyException(s"Invalid licence key provided ${licenceKey}")
    }
  }

}


object Test extends App {

  val lic = LicenceDetails(DateTime.now.withDate(2014, 10, 10), 165, "Softwaremill")
  println(LicenceEncryptor.encode(lic))
}