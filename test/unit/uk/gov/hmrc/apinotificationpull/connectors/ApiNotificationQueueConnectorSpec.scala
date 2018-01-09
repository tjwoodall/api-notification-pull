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

package uk.gov.hmrc.apinotificationpull.connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{verify => wverify, _}
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.HeaderNames._
import play.api.http.ContentTypes.XML
import play.api.http.Status._
import play.api.libs.json.Json.{stringify, toJson}
import uk.gov.hmrc.apinotificationpull.config.ServiceConfiguration
import uk.gov.hmrc.apinotificationpull.model.{Notification, Notifications}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec

class ApiNotificationQueueConnectorSpec extends UnitSpec with ScalaFutures with BeforeAndAfterEach
  with GuiceOneAppPerSuite with MockitoSugar {

  private val port = sys.env.getOrElse("WIREMOCK", "11114").toInt
  private val host = "localhost"
  private val apiNotificationQueueUrl = s"http://$host:$port"
  private val wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port))

  private class TestHttpClient extends HttpClient with WSHttp {
    override val hooks = Seq.empty
  }

  private val clientId = "client-id"

  trait Setup {
    val serviceConfiguration = mock[ServiceConfiguration]
    when(serviceConfiguration.baseUrl("api-notification-queue")).thenReturn(apiNotificationQueueUrl)

    implicit val hc = HeaderCarrier().withExtraHeaders("X-Client-ID" -> clientId)
    val connector = new ApiNotificationQueueConnector(serviceConfiguration, new TestHttpClient())
  }

  override def beforeEach() {
    wireMockServer.start()
    WireMock.configureFor(host, port)
  }

  override def afterEach() {
    wireMockServer.stop()
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

      val result = await(connector.getNotifications())

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
        stubFor(get(urlEqualTo(s"/notifications/$notificationId"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)))

        val result = await(connector.getById(notificationId))

        result shouldBe None
      }
    }

    "notification found" should {
      "return notification" in new Setup {
        val notificationPayload = "notification"
        val notification = Notification(notificationId, Map(CONTENT_TYPE -> XML), notificationPayload)

        stubFor(get(urlEqualTo(s"/notifications/$notificationId"))
            .withHeader(USER_AGENT, equalTo("api-notification-pull"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(notificationPayload)
              .withHeader(CONTENT_TYPE, XML)))

        val result = await(connector.getById(notificationId)).get

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
      val url: UrlPattern = urlEqualTo(s"/notifications/$notificationId")

      stubFor(delete(url).withHeader(USER_AGENT, equalTo("api-notification-pull"))
        .willReturn(aResponse().withStatus(OK)))

      await(connector.delete(notification))


      wverify(deleteRequestedFor(url))
    }
  }
}
