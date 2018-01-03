/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.apinotificationpull.acceptance

import java.util.UUID

import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}
import org.scalatest.OptionValues._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.test.FakeRequest
import play.api.test.Helpers._

class RetrieveAndDeleteNotificationSpec extends FeatureSpec with GivenWhenThen with Matchers with GuiceOneAppPerTest {

  feature("Retrieve a single message from the Pull Service (DELETE)") {
    info("As a 3rd Party")
    info("I want to successfully receive any notifications waiting for me")
    info("So that I can progress my original declaration submission")

    val notificationId = UUID.randomUUID()
    val clientId = "client-id"
    val xClientId = "X-Client-ID"
    val validRequest = FakeRequest("DELETE", s"/$notificationId").
      withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientId -> clientId)

    scenario("3rd party provides NotificationID but No message available/Matching NotificationID") {
      Given("a message has already been retrieved using the correct NotificationID")

      When("you make another call using the same MessageID")
      val result = route(app = app, validRequest).value

      Then("you will receive a 404 error response")
      status(result) shouldBe NOT_FOUND
    }

    scenario("Invalid Accept Header") {
      Given("you provide an invalid or missing Accept Header ")
      val request = validRequest.copyFakeRequest(headers = validRequest.headers.remove(ACCEPT))

      When("you call make the 'DELETE' with a NotificationID call to the api-notification-pull service")
      val result = route(app = app, request).value

      Then("you will be returned a 406 error response")
      status(result) shouldBe NOT_ACCEPTABLE
    }

    scenario("missing X-Client-Id Header") {
      Given("the platform does not inject a X-Client-Id Header")
      val request = validRequest.copyFakeRequest(headers = validRequest.headers.remove(xClientId))

      When("you call make the 'DELETE' with a NotificationID call to the api-notification-pull service ")
      val result = route(app = app, request).value

      Then("you will be returned a 500 error response")
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
