import sbt._

object AppDependencies {

  private val customsApiCommonVersion = "1.50.0"
  private val mockitoVersion = "3.3.3"
  private val scalaTestPlusPlayVersion = "3.1.3"
  private val wireMockVersion = "2.26.3"
  private val testScope = "test,component"

  val customsApiCommon = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion withSources()

  val mockito = "org.mockito" % "mockito-core" % mockitoVersion % testScope

  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % testScope

  val wireMock = "com.github.tomakehurst" % "wiremock-jre8" % wireMockVersion % testScope

  val customsApiCommonTests = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion % testScope classifier "tests"
}
