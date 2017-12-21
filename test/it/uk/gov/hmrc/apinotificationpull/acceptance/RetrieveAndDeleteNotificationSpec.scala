package uk.gov.hmrc.apinotificationpull.acceptance

import java.util.UUID

import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}
import org.scalatest.OptionValues._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._

class RetrieveAndDeleteNotificationSpec extends FeatureSpec with GivenWhenThen with Matchers with GuiceOneAppPerSuite {
  feature("Retrieve a single message from the Pull Service (DELETE)") {
    info("As a 3rd Party")
    info("I want to successfully receive any notifications waiting for me")
    info("So that I can progress my original declaration submission")

    scenario("3rd party provides NotificationID but No message available/Matching NotificationID") {
      Given("a message has already been retrieved using the correct NotificationID")
      val notificationID = UUID.randomUUID()

      When("you make another call using the same MessageID")
      val result = route(app = app, FakeRequest("DELETE", s"/$notificationID")).value

      Then("you will receive a 404 error response")
      status(result) shouldBe NOT_FOUND
    }
  }
}
