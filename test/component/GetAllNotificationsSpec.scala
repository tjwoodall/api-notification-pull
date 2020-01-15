/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import unit.util.RequestHeaders.X_CLIENT_ID_HEADER_NAME
import unit.util.RequestHeaders.{ACCEPT_HEADER, X_CLIENT_ID_HEADER}

class GetAllNotificationsSpec extends ComponentSpec with ExternalServices {


  val clientId: String = UUID.randomUUID().toString

  override def beforeAll(): Unit = {
    super.beforeAll()
    startMockServer()
  }

  override protected def beforeEach() {
    resetMockServer()
    stubForAllNotifications()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    stopMockServer()
  }


  feature("GET all notifications from the API Notification Pull service") {
    info("As a 3rd Party")
    info("I want to successfully retrieve all notification locations by client id")

    val validRequest = FakeRequest("GET", "/").
      withHeaders(ACCEPT_HEADER, X_CLIENT_ID_HEADER)

    scenario("Successful GET and 3rd party receives the notifications locations") {
      Given("There are notifications in the API Notification Queue")
      stubForAllNotifications()

      When("You call making the 'GET' action to the api-notification-pull service")
      val result = route(app, validRequest).value

      Then("You will receive all notifications for your client id")
      status(result) shouldBe OK

      val expectedBody = scala.xml.Utility.trim(
        <resource href="/notifications/">
          <link rel="self" href="/notifications/"/>
          <link rel="notification" href="/notifications/1234"/>
          <link rel="notification" href="/notifications/6789"/>
        </resource>
      )

      contentAsString(result).stripMargin shouldBe expectedBody.toString()

      And("The notifications will be retrieved")
      verify(getRequestedFor(urlMatching("/notifications")))
    }

    scenario("Missing Accept Header") {
      Given("You do not provide the Accept Header")
      val request = validRequest.withHeaders(validRequest.headers.remove(ACCEPT))

      When("You call make the 'GET' call to the api-notification-pull service")
      val result = route(app, request).value

      Then("You will be returned a 406 error response")
      status(result) shouldBe NOT_ACCEPTABLE
      contentAsString(result) shouldBe ""
    }

    scenario(s"Missing $X_CLIENT_ID_HEADER_NAME Header") {
      Given(s"The platform does not inject a $X_CLIENT_ID_HEADER_NAME Header")
      val request = validRequest.withHeaders(validRequest.headers.remove(X_CLIENT_ID_HEADER_NAME))

      When("You call make the 'GET' call to the api-notification-pull service ")
      val result = route(app, request).value

      Then("You will be returned a 500 error response")
      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentAsString(result) shouldBe ""
    }
  }

}
