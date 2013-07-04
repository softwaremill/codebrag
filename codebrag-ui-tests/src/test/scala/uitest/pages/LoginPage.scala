package uitest.pages

import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import uitest.commands.SeleniumCommands

class LoginPage(driver: WebDriver) {
  private val sc: SeleniumCommands = new SeleniumCommands(driver)
  val url = sc.URL + "login"

  @FindBy(css = "#login")
  val loginField: WebElement = null

  @FindBy(css = "#password")
  val passwordField: WebElement = null

  @FindBy(css = "button[type=submit]")
  val loginButton: WebElement = null

  @FindBy(css = ".login-error")
  val info: WebElement = null

  def login(login: String, password: String) {
    sc.waitForElementVisible(loginField)
    loginField.sendKeys(login)
    passwordField.sendKeys(password)
    loginButton.click()
    sc.waitForFinishLoading()
  }

  def openLoginPage() {
    driver.get(url)
    sc.waitForFinishLoading()
  }

  def getInfoText(): String = {
    sc.waitForElementVisible(info)
    return info.getText()
  }
}