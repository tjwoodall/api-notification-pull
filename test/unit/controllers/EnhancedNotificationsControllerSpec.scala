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

package unit.controllers

import java.util.UUID

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.http.HeaderNames.{ACCEPT, CONTENT_TYPE}
import play.api.http.MimeTypes
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.apinotificationpull.controllers.EnhancedNotificationsController
import uk.gov.hmrc.apinotificationpull.model.Notification
import uk.gov.hmrc.apinotificationpull.services.EnhancedApiNotificationQueueService
import uk.gov.hmrc.apinotificationpull.util.XmlBuilder
import uk.gov.hmrc.customs.api.common.config.ServicesConfig
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import unit.fakes.SuccessfulHeaderValidatorFake
import unit.util.StubCdsLogger
import unit.util.XmlUtil.string2xml

import scala.concurrent.Future

class EnhancedNotificationsControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar with BeforeAndAfterEach {

  private val mockEnhancedApiNotificationQueueService = mock[EnhancedApiNotificationQueueService]
  private val mockXmlBuilder = mock[XmlBuilder]
  private val mockLogger = new StubCdsLogger(mock[ServicesConfig])

  private val errorNotFoundXml = scala.xml.Utility.trim(
      <errorResponse>
        <code>NOT_FOUND</code>
        <message>Resource was not found</message>
      </errorResponse>
  )

  private val errorBadRequestXml = scala.xml.Utility.trim(
    <errorResponse>
      <code>BAD_REQUEST</code>
      <message>Notification has been read</message>
    </errorResponse>
  )

  private val errorInternalServerXml = scala.xml.Utility.trim(
    <errorResponse>
      <code>INTERNAL_SERVER_ERROR</code>
      <message>Internal server error</message>
    </errorResponse>
  )

  trait SetUp {
    implicit val materializer: Materializer = fakeApplication.materializer

    val xClientIdHeader = "X-Client-ID"
    val clientId = "client_id"

    val validHeaders = Seq(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> clientId)
    val headerValidator = new SuccessfulHeaderValidatorFake

    val controller = new EnhancedNotificationsController(mockEnhancedApiNotificationQueueService, headerValidator, mockXmlBuilder, mockLogger)

    val notificationId: String = UUID.randomUUID().toString
    val validRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("DELETE", s"/$notificationId").
      withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> "client-id")

    val headers = Map(CONTENT_TYPE -> MimeTypes.XML)
    val notification = Notification(notificationId, headers, "notification")
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockEnhancedApiNotificationQueueService, mockXmlBuilder)
  }

  "EnhancedNotificationsController" should {

    "return the unread notification" in new SetUp {

      when(mockEnhancedApiNotificationQueueService.getUnreadNotificationById(meq(notificationId))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(notification)))

      val result = await(controller.unread(notificationId).apply(validRequest))

      bodyOf(result) shouldBe "notification"
      status(result) shouldBe OK
    }

    "return a not found error for an unknown notification" in new SetUp {

      when(mockEnhancedApiNotificationQueueService.getUnreadNotificationById(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(new NotFoundException("not found exception"))))

      val result = await(controller.unread("unknown-notification-id").apply(validRequest))

      string2xml(bodyOf(result)) shouldBe errorNotFoundXml
      status(result) shouldBe NOT_FOUND

    }

    "return a bad request error for an already read notification" in new SetUp {

      when(mockEnhancedApiNotificationQueueService.getUnreadNotificationById(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(new BadRequestException("bad request exception"))))

      val result = await(controller.unread("read-notification-id").apply(validRequest))

      string2xml(bodyOf(result)) shouldBe errorBadRequestXml
      status(result) shouldBe BAD_REQUEST

    }

    "return an internal server error when an unexpected error is returned by downstream service" in new SetUp {

      when(mockEnhancedApiNotificationQueueService.getUnreadNotificationById(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(new InternalServerException("internal server exception"))))

      val result = await(controller.unread("internal-server-notification-id").apply(validRequest))

      string2xml(bodyOf(result)) shouldBe errorInternalServerXml
      status(result) shouldBe INTERNAL_SERVER_ERROR

    }

    "return an internal server error when an unexpected error occurs" in new SetUp {

      when(mockEnhancedApiNotificationQueueService.getUnreadNotificationById(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception("exception")))

      val result = await(controller.unread("internal-server-notification-id").apply(validRequest))

      string2xml(bodyOf(result)) shouldBe errorInternalServerXml
      status(result) shouldBe INTERNAL_SERVER_ERROR

    }
  }
}
