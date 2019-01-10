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

package unit.connectors

import com.github.tomakehurst.wiremock.client.WireMock.{verify => wverify, _}
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.ContentTypes.XML
import play.api.http.HeaderNames._
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, _}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json.{stringify, toJson}
import uk.gov.hmrc.apinotificationpull.connectors.ApiNotificationQueueConnector
import uk.gov.hmrc.apinotificationpull.model.{Notification, Notifications}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.play.bootstrap.http.{DefaultHttpClient, HttpClient}
import uk.gov.hmrc.play.test.UnitSpec
import util.ExternalServicesConfig.{Host, Port}
import util.WireMockRunner

class ApiNotificationQueueConnectorSpec extends UnitSpec with ScalaFutures with BeforeAndAfterEach with BeforeAndAfterAll
  with GuiceOneAppPerSuite with MockitoSugar with Eventually with WireMockRunner {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.api-notification-queue.host" -> Host,
      "microservice.services.api-notification-queue.port" -> Port
    ).overrides(
    bind[HttpClient].to[DefaultHttpClient]
  ).build()

  lazy val connector: ApiNotificationQueueConnector = app.injector.instanceOf[ApiNotificationQueueConnector]

  private val clientId = "client-id"

  trait Setup {

    implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("X-Client-ID" -> clientId)
  }

  override def beforeAll(): Unit = {
    startMockServer()
  }

  override def beforeEach() {
    resetMockServer()
  }

  override def afterAll() {
    stopMockServer()
  }

  "ApiNotificationQueueConnector.getNotifications()" should {

    "return the expected notifications from api-notification-queue " in new Setup {
      val notifications = Notifications(List("/notification/123", "/notification/456"))

      stubFor(get(urlEqualTo("/notifications"))
        .withHeader(USER_AGENT, equalTo("api-notification-pull"))
        .withHeader("X-Client-ID", equalTo(clientId))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(stringify(toJson(notifications))))
      )

      val result: Notifications = await(connector.getNotifications())

      result shouldBe notifications
    }

    "propagate the error if api-notification-queue fails with 500" in new Setup {
      stubFor(get(urlEqualTo("/notifications"))
        .withHeader(USER_AGENT, equalTo("api-notification-pull"))
        .willReturn(
          aResponse()
            .withStatus(INTERNAL_SERVER_ERROR)
            .withBody("Internal error.")
        )
      )

      intercept[Upstream5xxResponse] {
        await(connector.getNotifications())
      }
    }

    "propagate the error if api-notification-queue fails with 400" in new Setup {
      stubFor(get(urlEqualTo("/notifications"))
        .withHeader(USER_AGENT, equalTo("api-notification-pull"))
        .willReturn(
          aResponse()
            .withStatus(BAD_REQUEST)
            .withBody("X-Client-ID` header is not found in the request.")
        )
      )

      intercept[BadRequestException] {
        await(connector.getNotifications())
      }
    }
  }

  "ApiNotificationQueueConnector.getById(id: String)" when {
    val notificationId = "notificationId"

    "notification can't be found" should {
      "return none" in new Setup {
        stubFor(get(urlEqualTo(s"/notification/$notificationId"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)))

        val result: Option[Notification] = await(connector.getById(notificationId))

        result shouldBe None
      }
    }

    "notification found" should {
      "return notification" in new Setup {
        val notificationPayload = "notification"
        val notification = Notification(notificationId, Map(CONTENT_TYPE -> XML), notificationPayload)

        stubFor(get(urlEqualTo(s"/notification/$notificationId"))
            .withHeader(USER_AGENT, equalTo("api-notification-pull"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(notificationPayload)
              .withHeader(CONTENT_TYPE, XML)))

        val result: Notification = await(connector.getById(notificationId)).get

        result.id shouldBe notificationId
        result.payload shouldBe notification.payload
        result.headers should contain allElementsOf notification.headers
      }
    }
  }

  "ApiNotificationQueueConnector.delete(notification: Notification)" should {
    "delete the notification" in new Setup {
      val notificationId = "notificationId"
      val notification = Notification(notificationId, Map(CONTENT_TYPE -> XML), "payload")
      val url: UrlPattern = urlEqualTo(s"/notification/$notificationId")

      stubFor(delete(url).withHeader(USER_AGENT, equalTo("api-notification-pull"))
        .willReturn(aResponse().withStatus(OK)))

      await(connector.delete(notification))

      eventually {
        wverify(deleteRequestedFor(url))
      }
    }
  }
}
