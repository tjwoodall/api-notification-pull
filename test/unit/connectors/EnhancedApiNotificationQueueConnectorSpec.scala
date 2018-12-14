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

package unit.connectors

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.apinotificationpull.config.ServiceConfiguration
import uk.gov.hmrc.apinotificationpull.connectors.EnhancedApiNotificationQueueConnector
import uk.gov.hmrc.apinotificationpull.model.Notification
import uk.gov.hmrc.http.{NotFoundException, _}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.{ExecutionContext, Future}

class EnhancedApiNotificationQueueConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with Eventually {

  trait Setup {

    val X_CLIENT_ID_HEADER_NAME = "X-Client-ID"
    val clientId = "client-id"
    implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders(X_CLIENT_ID_HEADER_NAME -> clientId)

    val mockServiceConfiguration: ServiceConfiguration = mock[ServiceConfiguration]
    val mockHttpClient: HttpClient = mock[HttpClient]
    val mockHttpResponse = mock[HttpResponse]

    val notificationId = "some-notification-id"
    val headers = Map(X_CLIENT_ID_HEADER_NAME -> Seq(clientId))
    val notification = Notification(notificationId, headers.map(h => h._1 -> h._2.head), "notification-payload")

    val enhancedApiNotificationQueueConnector = new EnhancedApiNotificationQueueConnector(mockServiceConfiguration, mockHttpClient)

    when(mockServiceConfiguration.baseUrl("api-notification-queue")).thenReturn("http://api-notification-queue.url")
    when(mockHttpResponse.allHeaders).thenReturn(headers)
    when(mockHttpResponse.body).thenReturn("notification-payload")

  }

  "EnhancedApiNotificationQueueConnector" should {

    "return the unread notification for the specified notification id" in new Setup {

      when(mockHttpClient.GET[HttpResponse](meq(s"http://api-notification-queue.url/notifications/unread/$notificationId"))
        (any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])).thenReturn(Future.successful(mockHttpResponse))

      val result: Either[HttpException, Notification] = await(enhancedApiNotificationQueueConnector.getUnreadNotificationById(notificationId))

      result shouldBe Right(notification)
    }

    "return a not found exception when a downstream system returns a 404" in new Setup {

      private val notFoundException = new NotFoundException("not found exception")

      when(mockHttpClient.GET[HttpResponse](meq(s"http://api-notification-queue.url/notifications/unread/$notificationId"))
        (any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])).thenReturn(Future.failed(notFoundException))

      val result: Either[HttpException, Notification] = await(enhancedApiNotificationQueueConnector.getUnreadNotificationById(notificationId))

      result shouldBe Left(notFoundException)
    }

    "return a bad request exception when a downstream system returns a 400" in new Setup {

      private val badRequestException = new BadRequestException("bad request exception")

      when(mockHttpClient.GET[HttpResponse](meq(s"http://api-notification-queue.url/notifications/unread/$notificationId"))
        (any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])).thenReturn(Future.failed(badRequestException))

      val result: Either[HttpException, Notification] = await(enhancedApiNotificationQueueConnector.getUnreadNotificationById(notificationId))

      result shouldBe Left(badRequestException)
    }

    "return a internal server error exception when a downstream system returns any other error" in new Setup {

      private val unauthorisedException = new UnauthorizedException("unauthorised exception")

      when(mockHttpClient.GET[HttpResponse](meq(s"http://api-notification-queue.url/notifications/unread/$notificationId"))
        (any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any[ExecutionContext])).thenReturn(Future.failed(unauthorisedException))

      val result: Either[HttpException, Notification] = await(enhancedApiNotificationQueueConnector.getUnreadNotificationById(notificationId))

      result.left.get.message shouldBe "unauthorised exception"
      result.left.get.responseCode shouldBe 500
    }
  }
}
