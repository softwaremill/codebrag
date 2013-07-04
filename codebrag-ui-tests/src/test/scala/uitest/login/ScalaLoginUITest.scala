package uitest.login

import org.fest.assertions.Assertions
import uitest.CodebragUITest

class ScalaLoginUITest extends CodebragUITest {
  test("login") {
    loginPage.openLoginPage()
    loginPage.login(REGUSER, REGPASS)

    Assertions.assertThat(mainPage.isUserLogged("Fox Mulder")).isTrue()
    mainPage.logout()
  }

}
