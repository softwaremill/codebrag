package uitest.pages

import org.openqa.selenium.{Keys, By, WebElement, WebDriver}
import uitest.commands.SeleniumCommands
import org.openqa.selenium.support.FindBy

class CommitsPage(driver: WebDriver) {
  private val sc: SeleniumCommands = new SeleniumCommands(driver)
  val url = sc.URL + "commits"

  @FindBy(css = ".reviewed")
  val reviewedButton: WebElement = null

  @FindBy(css = ".icon-ok-circle")
  val quickReviewButton: WebElement = null

  @FindBy(css = "#commits-btn")
  val commitsButton: WebElement = null

  @FindBy(css = "#commits-btn .number")
  val commitsNbrElement: WebElement = null

  def openCommitsPage() {
    commitsButton.click()
    sc.waitForFinishLoading()
  }

  def quickReviewCommit(nbr: Int) {
    val commitElement: WebElement = driver.findElement(By.cssSelector(".commit-name:nth-of-type(" + nbr + ")"))
    commitElement.click()
    quickReviewButton.click()
    sc.waitForFinishLoading()
  }

  def openCommit(nbr: Int) {
    driver.findElement(By.xpath("//div[contains(@class,'commit-container')][" + nbr + "]")).click()
    sc.waitForFinishLoading()
  }

  def isCommitDetailDisplayed(): Boolean = {
    sc.waitForElementVisible(By.cssSelector(".diff-line-code"))
    return true
  }

  def markCommitAsReviewed() {
    val codeLine: WebElement = driver.findElement(By.cssSelector(".diff-line-code"))
    sc.mouseOverElement(codeLine)
    codeLine.sendKeys(Keys.PAGE_DOWN);

    reviewedButton.click()
    sc.waitForFinishLoading()
  }

  def getCommitsToReviewNbr(): Int = {
    sc.waitForElementVisible(commitsNbrElement)
    return commitsNbrElement.getText().toInt
  }

  def isPageOpen(): Boolean = {
    sc.waitForElementVisible(By.linkText("TO REVIEW"))
    return true
  }


}
