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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{status => wmStatus, _}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.OptionValues._
import org.scalatest._
import org.scalatest.concurrent.Eventually
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._

class RetrieveAndDeleteNotificationSpec extends FeatureSpec with GivenWhenThen with Matchers with GuiceOneAppPerTest
  with BeforeAndAfterEach with BeforeAndAfterAll with Eventually {

  private val externalServicesHost = "localhost"
  private val externalServicesPort = 11111

  override def newAppForTest(testData: TestData): Application = new GuiceApplicationBuilder().configure(Map(
    "microservice.services.api-notification-queue.host" -> externalServicesHost,
    "microservice.services.api-notification-queue.port" -> externalServicesPort
  )).build()

  val externalServices: WireMockServer = new WireMockServer(wireMockConfig.port(externalServicesPort))

  override def beforeAll(): Unit = {
    super.beforeAll()
    if(!externalServices.isRunning) externalServices.start()
    WireMock.configureFor(externalServicesHost, externalServicesPort)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    externalServices.stop()
  }

  feature("Retrieve(DELETE) a single notification from the API Notification Pull service") {
    info("As a 3rd Party")
    info("I want to successfully retrieve a notification waiting for me")
    info("So that I can progress my original declaration submission")

    val notificationId = UUID.randomUUID.toString
    val clientId = "client-id"
    val xClientId = "X-Client-ID"
    val validRequest = FakeRequest("DELETE", s"/$notificationId").
      withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientId -> clientId)

    scenario("Successful DELETE and 3rd party receives the notification") {
      Given("There is a notification waiting for you in the Notification Queue and you have the correct notification Id")
      val notificationBody = "<notification>notification</notification>"
      stubForExistingNotification(notificationId, notificationBody)

      When("You call making the 'DELETE' action to the api-notification-pull service")
      val result = route(app = app, validRequest).value

      Then("You will receive the notification")
      status(result) shouldBe OK
      contentAsString(result).stripMargin shouldBe notificationBody

      And("The notification will be DELETED")
      verify(eventually(deleteRequestedFor(urlMatching(s"/notifications/$notificationId"))))
    }

    scenario("3rd party provides NotificationID but No notification available/Matching NotificationID") {
      Given("A notification has already been retrieved using the correct NotificationID")

      stubFor(get(urlMatching(s"/notifications/$notificationId"))
        .willReturn(aResponse()
          .withStatus(NOT_FOUND)))

      When("You make another call using the same notification Id")
      val result = route(app = app, validRequest).value

      Then("You will receive a 404 error response")
      status(result) shouldBe NOT_FOUND
      contentAsString(result) shouldBe "NOT FOUND"
    }

    scenario("Invalid Accept Header") {
      Given("You provide an invalid or missing Accept Header ")
      val request = validRequest.copyFakeRequest(headers = validRequest.headers.remove(ACCEPT))

      When("You call make the 'DELETE' call, with a notification Id, to the api-notification-pull service")
      val result = route(app = app, request).value

      Then("You will be returned a 406 error response")
      status(result) shouldBe NOT_ACCEPTABLE
      contentAsString(result) shouldBe ""
    }

    scenario("Missing X-Client-Id Header") {
      Given("The platform does not inject a X-Client-Id Header")
      val request = validRequest.copyFakeRequest(headers = validRequest.headers.remove(xClientId))

      When("You call make the 'DELETE' call, with a notification Id, to the api-notification-pull service ")
      val result = route(app = app, request).value

      Then("You will be returned a 500 error response")
      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentAsString(result) shouldBe ""
    }
  }

  private def stubForExistingNotification(notificationId: String, notificationBody: String) = {
    stubFor(get(urlMatching(s"/notifications/$notificationId"))
      .willReturn(aResponse()
        .withBody(notificationBody)
        .withStatus(OK)))

    stubFor(delete(urlMatching(s"/notifications/$notificationId"))
      .willReturn(aResponse()
        .withStatus(OK)))
  }
}
