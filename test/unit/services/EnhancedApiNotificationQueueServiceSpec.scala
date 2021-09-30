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

package unit.services

import java.util.UUID.fromString
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.apinotificationpull.connectors.EnhancedApiNotificationQueueConnector
import uk.gov.hmrc.apinotificationpull.model.NotificationStatus._
import uk.gov.hmrc.apinotificationpull.model.{Notification, Notifications}
import uk.gov.hmrc.apinotificationpull.services.EnhancedApiNotificationQueueService
import uk.gov.hmrc.http.HeaderCarrier
import unit.util.RequestHeaders.X_CLIENT_ID_HEADER
import util.UnitSpec

import scala.concurrent.Future

class EnhancedApiNotificationQueueServiceSpec extends UnitSpec with MockitoSugar with Eventually {

  private val hc = HeaderCarrier()

  val notificationId = "some-notification-id"
  val conversationId = fromString("6e97f91f-f0c0-43bd-a3e0-cfa3dbc0df4f")

  trait Setup {
    val mockEnhancedApiNotificationQueueConnector = mock[EnhancedApiNotificationQueueConnector]
    val enhancedApiNotificationQueueService = new EnhancedApiNotificationQueueService(mockEnhancedApiNotificationQueueConnector)
  }

  "EnhancedApiNotificationQueueService" should {

    "pass the unpulled notification id to the connector" in new Setup {

      val headers = Map(X_CLIENT_ID_HEADER)
      val notification = Notification(notificationId, headers, "notification-payload")

      when(mockEnhancedApiNotificationQueueConnector.getNotificationBy(notificationId, Unpulled)(hc)).thenReturn(Future.successful(Right(notification)))

      val result = (enhancedApiNotificationQueueService.getNotificationBy(notificationId, Unpulled)(hc)).futureValue

      result shouldBe Right(notification)
    }

    "pass the pulled notification id to the connector" in new Setup {

      val headers = Map(X_CLIENT_ID_HEADER)
      val notification = Notification(notificationId, headers, "notification-payload")

      when(mockEnhancedApiNotificationQueueConnector.getNotificationBy(notificationId, Pulled)(hc)).thenReturn(Future.successful(Right(notification)))

      val result = (enhancedApiNotificationQueueService.getNotificationBy(notificationId, Pulled)(hc)).futureValue

      result shouldBe Right(notification)
    }

    "pass the conversation id to the connector" in new Setup {

      when(mockEnhancedApiNotificationQueueConnector.getAllNotificationsBy(conversationId)(hc)).thenReturn(Future.successful(Notifications(List())))

      val result = (enhancedApiNotificationQueueService.getAllNotificationsBy(conversationId)(hc)).futureValue

      result shouldBe Notifications(List())
    }

    "pass the conversation id and notification status to the connector" in new Setup {

      when(mockEnhancedApiNotificationQueueConnector.getAllNotificationsBy(conversationId, Unpulled)(hc)).thenReturn(Future.successful(Notifications(List())))

      val result = (enhancedApiNotificationQueueService.getAllNotificationsBy(conversationId, Unpulled)(hc)).futureValue

      result shouldBe Notifications(List())
    }

    "pass the notification status to the connector" in new Setup {

      when(mockEnhancedApiNotificationQueueConnector.getAllNotificationsBy(Pulled)(hc)).thenReturn(Future.successful(Notifications(List())))

      val result = (enhancedApiNotificationQueueService.getAllNotificationsBy(Pulled)(hc)).futureValue

      result shouldBe Notifications(List())
    }
  }
}
