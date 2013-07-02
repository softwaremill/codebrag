package uitest.commits

import uitest.CodebragUITest
import org.fest.assertions.Assertions

class QuickReviewUITest extends CodebragUITest {
  test("quick review") {
    loginPage.openLoginPage()
    loginPage.login(REGUSER, REGPASS)

    val commitsNbr: Int = commitsPage.getCommitsToReviewNbr()
    commitsPage.quickReviewCommit(1)
    Assertions.assertThat(commitsPage.getCommitsToReviewNbr()).isEqualTo(commitsNbr - 1)
  }
}
