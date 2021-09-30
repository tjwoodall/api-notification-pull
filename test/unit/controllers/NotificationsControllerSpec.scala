/*
 * Copyright 2021 HM Revenue & Customs
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

package unit.controllers

import java.util.UUID
import java.util.concurrent.TimeoutException
import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.MimeTypes
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Results.Ok
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.apinotificationpull.controllers.NotificationsController
import uk.gov.hmrc.apinotificationpull.model.{Notification, Notifications}
import uk.gov.hmrc.apinotificationpull.presenters.NotificationPresenter
import uk.gov.hmrc.apinotificationpull.services.ApiNotificationQueueService
import uk.gov.hmrc.apinotificationpull.util.XmlBuilder
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.fakes.SuccessfulHeaderValidatorFake
import unit.util.RequestHeaders.{ACCEPT_HEADER, X_CLIENT_ID_HEADER}
import unit.util.XmlUtil.string2xml
import unit.util.{MaterializerSupport, StubNotificationLogger}
import util.UnitSpec

import scala.concurrent.Future

class NotificationsControllerSpec extends UnitSpec with MaterializerSupport with MockitoSugar with BeforeAndAfterEach {

  private implicit val ec = Helpers.stubControllerComponents().executionContext
  private val mockApiNotificationQueueService = mock[ApiNotificationQueueService]
  private val notificationPresenter = mock[NotificationPresenter]
  private val mockXmlBuilder = mock[XmlBuilder]
  private val mockLogger = new StubNotificationLogger(new CdsLogger(mock[ServicesConfig]))

  private val errorXml = scala.xml.Utility.trim(
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

  trait Setup {
    val clientId = "client_id"

    val validHeaders = Seq(ACCEPT_HEADER, X_CLIENT_ID_HEADER)
    val headerValidator = new SuccessfulHeaderValidatorFake(new StubNotificationLogger(new CdsLogger(mock[ServicesConfig])), Helpers.stubControllerComponents())

    val controller = new NotificationsController(mockApiNotificationQueueService, headerValidator, notificationPresenter, mockXmlBuilder, Helpers.stubControllerComponents(), mockLogger)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(notificationPresenter, mockApiNotificationQueueService, mockXmlBuilder)
  }

  "delete notification by id" should {
    trait SetupDeleteNotification extends Setup {
      val notificationId: String = UUID.randomUUID().toString
      val validRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("DELETE", s"/$notificationId").
        withHeaders(ACCEPT_HEADER, X_CLIENT_ID_HEADER)

      val presentedNotification = Ok("presented notification")
      val headers = Map(CONTENT_TYPE -> MimeTypes.XML)
      val notification = Notification(notificationId, headers, "notification")
    }

    "return the presented notification" in new SetupDeleteNotification {
      when(mockApiNotificationQueueService.getAndRemoveNotification(meq(notificationId))(any[HeaderCarrier]))
        .thenReturn(Some(notification))

      when(notificationPresenter.present(Some(notification))).thenReturn(presentedNotification)

      val result = controller.delete(notificationId).apply(validRequest).futureValue

      result shouldBe presentedNotification
    }

    "fail if ApiNotificationQueueService failed" in new SetupDeleteNotification {
      when(mockApiNotificationQueueService.getAndRemoveNotification(anyString)(any(classOf[HeaderCarrier])))
        .thenReturn(Future.failed(new TimeoutException()))

      val result = (controller.delete(notificationId).apply(validRequest)).futureValue

      status(result) shouldBe INTERNAL_SERVER_ERROR
      string2xml(bodyOf(result)) shouldBe errorXml
    }
  }

  "get all notifications" should {

    trait SetupGetAllNotifications extends Setup {

      protected val notifications = Notifications(List(s"/notifications/1"))

      val validRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/").withHeaders(validHeaders: _*)
    }

    "return all notifications" in new SetupGetAllNotifications {
      val xmlNotifications = <resource href="/notifications/"><link rel="notification" href="/notifications/1"/></resource>

      when(mockApiNotificationQueueService.getNotifications()(any(classOf[HeaderCarrier])))
        .thenReturn(Future.successful(notifications))

      when(mockXmlBuilder.toXml(notifications)).thenReturn(xmlNotifications)

      val result = controller.getAll().apply(validRequest).futureValue

      status(result) shouldBe OK

      string2xml(bodyOf(result)) shouldBe xmlNotifications
    }

    "fail if ApiNotificationQueueService failed" in new SetupGetAllNotifications {
      when(mockApiNotificationQueueService.getNotifications()(any(classOf[HeaderCarrier])))
        .thenReturn(Future.failed(new TimeoutException()))

      val result = controller.getAll().apply(validRequest).futureValue

      status(result) shouldBe INTERNAL_SERVER_ERROR

      string2xml(bodyOf(result)) shouldBe errorXml
    }
  }

}
