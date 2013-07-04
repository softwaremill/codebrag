package uitest.pages

import org.openqa.selenium.{WebElement, WebDriver}
import uitest.commands.SeleniumCommands
import org.openqa.selenium.support.FindBy

class RegistrationPage(driver: WebDriver) {
  private val sc: SeleniumCommands = new SeleniumCommands(driver)
  val url = sc.URL + "register"

  @FindBy(css = "#reg-login")
  val loginField: WebElement = null

  @FindBy(css = "#email")
  val emailField: WebElement = null

  @FindBy(css = "#reg-password")
  val passwordField: WebElement = null

  @FindBy(css = "#repassword")
  val repasswordField: WebElement = null

  @FindBy(css = "#reg-btn")
  val registerButton: WebElement = null

  def register(login: String, email: String, password: String) {
    loginField.sendKeys(login)
    emailField.sendKeys(email)
    passwordField.sendKeys(password)
    repasswordField.sendKeys(password)
    registerButton.click()
    sc.waitForFinishLoading()
  }

  def openRegistrationPage() {
    driver.get(url)
    sc.waitForFinishLoading()
  }

}
