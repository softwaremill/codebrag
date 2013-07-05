package uitest.login

import org.fest.assertions.Assertions
import uitest.CodebragUITest

class ScalaLoginUITest extends CodebragUITest {
  test("login") {
    loginPage.openLoginPage()
    Thread.sleep(10000)
    loginPage.login(REGUSER, REGPASS)

    Assertions.assertThat(mainPage.isUserLogged("Fox Mulder")).isTrue()
    mainPage.logout()
  }

}
