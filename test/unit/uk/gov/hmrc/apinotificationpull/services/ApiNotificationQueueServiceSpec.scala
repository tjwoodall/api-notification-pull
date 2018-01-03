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

package uk.gov.hmrc.apinotificationpull.services

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.apinotificationpull.connectors.ApiNotificationQueueConnector
import uk.gov.hmrc.apinotificationpull.model.Notifications
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class ApiNotificationQueueServiceSpec extends UnitSpec with MockitoSugar {

  private val hc = HeaderCarrier()

  private val notifications = Notifications(List("/notification/123", "/notification/456"))

  trait Setup {
    val mockApiNotificationQueueConnector = mock[ApiNotificationQueueConnector]
    val apiNotificationQueueService = new ApiNotificationQueueService(mockApiNotificationQueueConnector)
  }

  "getNotifications" should {

    "return the expected notifications from the `api-notification-queue` connector" in new Setup {
      when(mockApiNotificationQueueConnector.getNotifications()(hc)).thenReturn(Future.successful(notifications))
      await(apiNotificationQueueService.getNotifications()(hc)) shouldBe notifications
      verify(mockApiNotificationQueueConnector).getNotifications()(hc)
    }

  }

}
