package com.softwaremill.codebrag.service.config

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.typesafe.config.ConfigFactory

class MultiRepoConfigSpec extends FlatSpec with ShouldMatchers {

  val ConfigContent =
    """
      |repos {
      |   firstRepo: {
      |     username: "john"
      |     password: "secret"
      |   }
      |   
      |   secondRepo: {}
      |   
      |   thirdRepo: {
      |     passphrase: "123abc"
      |   }
      |
      |}
    """.stripMargin

  val config = new MultiRepoConfig {
    override def rootConfig = ConfigFactory.parseString(ConfigContent)
  }

  it should "have configurations for all listed repos" in {
    // when
    val repoConfigs = config.repositoriesConfig

    // then
    repoConfigs.keySet should be(Set("firstRepo", "secondRepo", "thirdRepo"))
  }

  it should "have proper configuration data for all listed repos" in {
    // when
    val repoConfigs = config.repositoriesConfig

    // then
    repoConfigs("firstRepo") should be(RepoCredentials("firstRepo", Some("john"), Some("secret"), None))
    repoConfigs("secondRepo") should be(RepoCredentials("secondRepo", None, None, None))
    repoConfigs("thirdRepo") should be(RepoCredentials("thirdRepo", None, None, Some("123abc")))
  }

}
