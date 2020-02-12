import sbt._

object AppDependencies {

  private val customsApiCommonVersion = "1.46.0"
  private val hmrcTestVersion = "3.9.0-play-26"
  private val mockitoVersion = "3.2.4"
  private val scalaTestPlusPlayVersion = "3.1.3"
  private val wireMockVersion = "2.26.0"
  private val testScope = "test,it"

  val customsApiCommon = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion withSources()

  val hmrcTest = "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % testScope

  val mockito = "org.mockito" % "mockito-core" % mockitoVersion % testScope

  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % testScope

  val wireMock = "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % testScope

  val customsApiCommonTests = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion % testScope classifier "tests"
}
