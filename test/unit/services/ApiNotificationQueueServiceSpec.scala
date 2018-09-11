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

package unit.services

import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.Eventually
import org.scalatest.mockito.MockitoSugar
import play.api.http.ContentTypes.XML
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status.OK
import uk.gov.hmrc.apinotificationpull.connectors.ApiNotificationQueueConnector
import uk.gov.hmrc.apinotificationpull.model.{Notification, Notifications}
import uk.gov.hmrc.apinotificationpull.services.ApiNotificationQueueService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class ApiNotificationQueueServiceSpec extends UnitSpec with MockitoSugar with Eventually {

  private val hc = HeaderCarrier()

  private val notifications = Notifications(List("/notification/123", "/notification/456"))

  trait Setup {
    val mockApiNotificationQueueConnector: ApiNotificationQueueConnector = mock[ApiNotificationQueueConnector]
    val apiNotificationQueueService: ApiNotificationQueueService = new ApiNotificationQueueService(mockApiNotificationQueueConnector)
  }

  "getNotifications" should {

    "return the expected notifications from the `api-notification-queue` connector" in new Setup {
      when(mockApiNotificationQueueConnector.getNotifications()(hc)).thenReturn(Future.successful(notifications))
      await(apiNotificationQueueService.getNotifications()(hc)) shouldBe notifications
      verify(mockApiNotificationQueueConnector).getNotifications()(hc)
    }

  }

  "getAndRemoveNotification(notificationId: String)" when {

    "the notification exists and is successfully retrieved and deleted" should {
      trait GetAndRemoveExistingNotification extends Setup {
        val notificationId: String = "notificationId"
        val notification: Notification = Notification(notificationId, Map(CONTENT_TYPE -> XML), "notification")
        when(mockApiNotificationQueueConnector.getById(meq(notificationId))(any[HeaderCarrier])).thenReturn(Some(notification))
        when(mockApiNotificationQueueConnector.delete(meq(notification))(any[HeaderCarrier])).thenReturn(Future.successful(HttpResponse(OK)))

        val result: Option[Notification] = await(apiNotificationQueueService.getAndRemoveNotification(notificationId)(HeaderCarrier()))
      }

      "return the notification" in new GetAndRemoveExistingNotification {
        result shouldBe Some(notification)
      }

      "delete the notification" in new GetAndRemoveExistingNotification {
        eventually(verify(mockApiNotificationQueueConnector).delete(meq(notification))(any[HeaderCarrier]))
      }
    }

    "the notification exists, is retrieved and deletion fails" should {
      trait GetAndFailRemovingExistingNotification extends Setup {
        val notificationId: String = "notificationId"
        val notification: Notification = Notification(notificationId, Map(CONTENT_TYPE -> XML), "notification")
        when(mockApiNotificationQueueConnector.getById(meq(notificationId))(any[HeaderCarrier])).thenReturn(Some(notification))
        when(mockApiNotificationQueueConnector.delete(meq(notification))(any[HeaderCarrier])).thenThrow(new RuntimeException())
      }

      "return a failed future" in new GetAndFailRemovingExistingNotification {
        intercept[RuntimeException] {
          await(apiNotificationQueueService.getAndRemoveNotification(notificationId)(HeaderCarrier()))
        }
      }
    }

    "the notification does not exist" should {
      trait GetAndRemoveNoNotification extends Setup {
        val notificationId: String = "notificationId"
        when(mockApiNotificationQueueConnector.getById(meq(notificationId))(any[HeaderCarrier])).thenReturn(None)

        val result: Option[Notification] = await(
          apiNotificationQueueService.getAndRemoveNotification(notificationId)(HeaderCarrier()))
      }

      "return None" in new GetAndRemoveNoNotification {
        result shouldBe None
      }

      "not delete the notification if it doesn't exist" in new GetAndRemoveNoNotification {
        verify(mockApiNotificationQueueConnector, never()).delete(any[Notification])(any[HeaderCarrier])
      }
    }
  }
}
