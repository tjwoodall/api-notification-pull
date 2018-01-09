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

package uk.gov.hmrc.apinotificationpull.controllers

import java.util.concurrent.TimeoutException

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.test.FakeRequest
import uk.gov.hmrc.apinotificationpull.fakes.SuccessfulHeaderValidatorFake
import uk.gov.hmrc.apinotificationpull.model.Notifications
import uk.gov.hmrc.apinotificationpull.services.ApiNotificationQueueService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.xml.{Node, Utility, XML}
import scala.concurrent.Future
import scala.util.control.NonFatal

class NotificationsControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  private val notificationId1 = 1234
  private val notificationId2 = 6789

  private val notifications = Notifications(List(s"/notifications/$notificationId1", s"/notifications/$notificationId2"))

  private val xClientIdHeader = "X-Client-ID"
  private val clientId = "client_id"

  private val validHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> clientId)

  trait Setup {
    implicit val materializer = fakeApplication.materializer

    val headerValidator = new SuccessfulHeaderValidatorFake

    val mockApiNotificationQueueService = mock[ApiNotificationQueueService]
    val controller = new NotificationsController(mockApiNotificationQueueService, headerValidator)
  }

  "delete notification" should {

    val validRequest = FakeRequest("DELETE", s"/$notificationId1").withHeaders(validHeaders: _*)

    "return 404 NOT_FOUND response when the notification does not exist" in new Setup {
        val result = await(controller.delete(notificationId1.toString).apply(validRequest))

        status(result) shouldBe NOT_FOUND
        bodyOf(result) shouldBe ""
      }
    }

  "get all notifications" should {

    val validRequest = FakeRequest("GET", "/").withHeaders(validHeaders: _*)

    "return all notifications" in new Setup {
      when(mockApiNotificationQueueService.getNotifications()(any(classOf[HeaderCarrier])))
        .thenReturn(Future.successful(notifications))

      val result = await(controller.getAll().apply(validRequest))

      status(result) shouldBe OK

      val expectedXml = scala.xml.Utility.trim(
        <resource href="/notifications/">
          <link rel="self" href="/notifications/"/>
          <link rel="notification" href="/notifications/1234"/>
          <link rel="notification" href="/notifications/6789"/>
        </resource>
      )

      string2xml(bodyOf(result)) shouldBe expectedXml
    }

    "fail if ApiNotificationQueueService failed" in new Setup {
      when(mockApiNotificationQueueService.getNotifications()(any(classOf[HeaderCarrier])))
        .thenReturn(Future.failed(new TimeoutException()))

      val result = await(controller.getAll().apply(validRequest))

      status(result) shouldBe INTERNAL_SERVER_ERROR

      val expectedXml = scala.xml.Utility.trim(
        <error_response>
          <code>UNKNOWN_ERROR</code>
          <errors>
            <error>
              <type>SERVICE_UNAVAILABLE</type>
              <description>An unexpected error occurred</description>
            </error>
          </errors>
        </error_response>
      )

      string2xml(bodyOf(result)) shouldBe expectedXml
    }
  }

  protected def string2xml(s: String): Node = {
    val xml = try {
      XML.loadString(s)
    } catch {
      case NonFatal(t) => fail("Not an XML: " + s, t)
    }

    Utility.trim(xml)
  }

}
