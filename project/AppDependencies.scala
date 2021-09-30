import sbt._

object AppDependencies {

  private val customsApiCommonVersion = "1.57.0"
  private val mockitoVersion = "3.11.1"
  private val scalaTestPlusPlayVersion = "5.1.0"
  private val wireMockVersion = "2.27.1"
  private val testScope = "test,component"

  val customsApiCommon = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion withSources()

  val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusPlayVersion % testScope

  val wireMock = "com.github.tomakehurst" % "wiremock-standalone" % wireMockVersion % testScope

  val flexmark = "com.vladsch.flexmark" % "flexmark-all" % "0.35.10"  % testScope

  val mockito = "org.scalatestplus" %% "mockito-3-4" % "3.2.9.0" % testScope

  val customsApiCommonTests = "uk.gov.hmrc" %% "customs-api-common" % customsApiCommonVersion % testScope classifier "tests"

  val silencerPlugin = compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.5" cross CrossVersion.full)

  val silencerLib = "com.github.ghik" % "silencer-lib" % "1.7.5" % Provided cross CrossVersion.full
}
