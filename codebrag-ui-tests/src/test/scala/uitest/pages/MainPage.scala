package uitest.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import uitest.commands.SeleniumCommands

class MainPage(driver: WebDriver) {
  private val sc: SeleniumCommands = new SeleniumCommands(driver)

  @FindBy(linkText = "Logout")
  val logoutLink: WebElement = null

  def logout() {
    logoutLink.click()
    sc.waitForFinishLoading()
  }

  def isUserLogged(user: String): Boolean = {
    sc.waitForElementVisible(By.linkText("Signed in as " + user))
    return true
  }
}
