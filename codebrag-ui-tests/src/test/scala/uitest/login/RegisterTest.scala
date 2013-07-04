package uitest.login

import uitest.CodebragUITest
import org.fest.assertions.Assertions
import pl.softwaremill.common.util.RichString
import uitest.pages.RegistrationPage
import org.openqa.selenium.support.PageFactory

class RegisterTest extends CodebragUITest {
    final val LOGIN = RichString.generateRandom(5)
    final val EMAIL = LOGIN + "@example.org"
    final val PASSWORD = "test"

    test("register") {
      val registrationPage: RegistrationPage = PageFactory.initElements(driver, classOf[RegistrationPage])

      registrationPage.openRegistrationPage()
      registrationPage.register(LOGIN, EMAIL, PASSWORD)
      Assertions.assertThat(loginPage.getInfoText).contains("Registration was successful!")

      loginPage.login(EMAIL, REGPASS)
      Assertions.assertThat(mainPage.isUserLogged(LOGIN)).isTrue()
      mainPage.logout()
    }
}
