import sbt._

object AppDependencies {

  private val customsApiCommonVersion = "1.36.0"
  private val hmrcTestVersion = "3.5.0-play-25"
  private val scalaTestVersion = "3.0.5"
  private val mockitoVersion = "2.24.5"
  private val scalaTestPlusPlayVersion = "2.0.1"
  private val wireMockVersion = "2.21.0"
  private val testScope = "test,it"

  val customsApiCommon = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion withSources()
  val hmrcTest = "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % testScope
  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % testScope
  val mockito = "org.mockito" % "mockito-core" % mockitoVersion % testScope
  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % testScope
  val wireMock = "com.github.tomakehurst" % "wiremock" % wireMockVersion exclude("org.apache.httpcomponents","httpclient") exclude("org.apache.httpcomponents","httpcore")
  val customsApiCommonTests = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion % testScope classifier "tests"
}
