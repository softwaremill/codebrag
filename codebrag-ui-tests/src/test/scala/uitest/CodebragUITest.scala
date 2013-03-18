package uitest

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import javax.servlet.ServletContext
import org.openqa.selenium.firefox.FirefoxDriver
import java.util.concurrent.TimeUnit
import com.softwaremill.codebrag.Beans
import pages.{MainPage, LoginPage}
import org.openqa.selenium.support.PageFactory

class CodebragUITest extends FunSuite with EmbeddedJetty with BeforeAndAfterAll with BeforeAndAfter {
  final val REGUSER = "reguser"
  final val REGPASS = "regpass"
  final val REGMAIL = "reguser@regmail.pl"

  var driver: FirefoxDriver = _
  var loginPage: LoginPage = _
  var mainPage: MainPage = _
  var beans: Beans = _

  override def beforeAll() {
    startJetty()
    beans = context.getAttribute("codebrag").asInstanceOf[Beans]
  }

  before {
    driver = new FirefoxDriver()
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)
    loginPage = PageFactory.initElements(driver, classOf[LoginPage])
    mainPage = PageFactory.initElements(driver, classOf[MainPage])
  }

  after {
    driver.close
    driver = null
  }

  override def afterAll() {
    stopJetty()
  }
}

trait EmbeddedJetty {
  protected var jetty: Server = null
  protected var context: ServletContext = null

  def startJetty() {
    jetty = new Server(8080)
    jetty setHandler prepareContext
    jetty.start()
  }

  private def prepareContext() = {
    val context = new WebAppContext()
    context setContextPath "/"
    context setResourceBase "codebrag-ui/src/main/webapp"
    this.context = context.getServletContext
    context
  }


  def stopJetty() {
    jetty.stop()
  }
}