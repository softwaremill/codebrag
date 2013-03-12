package uitest

import org.fest.assertions.Assertions

class ScalaLoginUITest extends CodebragUITest {
  test("login") {
    loginPage.openLoginPage()
    loginPage.login(REGUSER, REGPASS)

    Assertions.assertThat(messagesPage.isUserLogged(REGUSER)).isTrue()
    messagesPage.logout()
  }

}
