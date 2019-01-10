/*
 * Copyright 2019 HM Revenue & Customs
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

package component

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock.{status => _, _}
import org.scalatest.OptionValues._
import org.scalatest.concurrent.Eventually
import play.api.test.FakeRequest
import play.api.test.Helpers._

class RetrieveAndDeleteNotificationSpec extends ComponentSpec with Eventually with ExternalServices {

  override val clientId: String = UUID.randomUUID.toString

  private val notificationId = UUID.randomUUID.toString

  override def beforeAll(): Unit = {
    startMockServer()
  }

  override protected def beforeEach() {
    resetMockServer()
  }

  override def afterAll(): Unit = {
    stopMockServer()
  }

  feature("Retrieve(DELETE) a single notification from the API Notification Pull service") {
    info("As a 3rd Party")
    info("I want to successfully retrieve a notification waiting for me")
    info("So that I can progress my original declaration submission")

    val validRequest = FakeRequest("DELETE", s"/$notificationId").
      withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> clientId)

    scenario("Successful DELETE and 3rd party receives the notification") {
      Given("There is a notification waiting in the API Notification Queue and you have the correct notification Id")
      val header1 = "header1-name" -> "header1-val"
      val header2 = "header2-name" -> "header2-val"
      val notificationBody = "<notification>notification</notification>"
      stubForExistingNotificationForDelete(notificationId, notificationBody, Seq(header1, header2))

      When("You call making the 'DELETE' action to the api-notification-pull service")
      val result = route(app, validRequest).value

      Then("You will receive the notification")
      status(result) shouldBe OK
      contentAsString(result).stripMargin shouldBe notificationBody
      await(result).header.headers should contain allOf(header1, header2)

      And("The notification will be DELETED")
      verify(eventually(deleteRequestedFor(urlMatching(s"/notification/$notificationId"))))
    }

    scenario("3rd party provides notification Id but there are no notifications available or matching the Notification Id") {
      Given("A notification has already been retrieved using the correct notification Id")

      stubFor(get(urlMatching(s"/notification/$notificationId"))
        .willReturn(aResponse()
          .withStatus(NOT_FOUND)))

      When("You make another call using the same notification Id")
      val result = route(app, validRequest).value

      Then("You will receive a 404 error response")
      status(result) shouldBe NOT_FOUND
      contentAsString(result) shouldBe "NOT FOUND"
    }

    scenario("Invalid Accept Header") {
      Given("You do not provide the Accept Header")
      val request = validRequest.copyFakeRequest(headers = validRequest.headers.remove(ACCEPT))

      When("You call make the 'DELETE' call, with a notification Id, to the api-notification-pull service")
      val result = route(app, request).value

      Then("You will be returned a 406 error response")
      status(result) shouldBe NOT_ACCEPTABLE
      contentAsString(result) shouldBe ""
    }

    scenario("Missing X-Client-Id Header") {
      Given("The platform does not inject a X-Client-Id Header")
      val request = validRequest.copyFakeRequest(headers = validRequest.headers.remove(xClientIdHeader))

      When("You call make the 'DELETE' call, with a notification Id, to the api-notification-pull service ")
      val result = route(app, request).value

      Then("You will be returned a 500 error response")
      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentAsString(result) shouldBe ""
    }
  }

}
