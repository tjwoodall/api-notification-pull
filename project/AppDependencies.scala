import sbt.*

object AppDependencies {

  val playVersion = "play-30"
  val bootstrap = "10.4.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% s"bootstrap-backend-$playVersion"   % bootstrap
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                   %% s"bootstrap-test-$playVersion"    % bootstrap    % Test,
    "org.wiremock"                   % "wiremock-standalone"             % "3.13.1"     % Test,
    "com.vladsch.flexmark"           % "flexmark-all"                    % "0.64.8"     % Test,
    "org.scalatestplus"             %% "mockito-3-4"                     % "3.2.10.0"   % Test,
    "org.scalatestplus.play"        %% "scalatestplus-play"              % "7.0.2"      % Test,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"            % "2.20.1"     % Test
  )
}
