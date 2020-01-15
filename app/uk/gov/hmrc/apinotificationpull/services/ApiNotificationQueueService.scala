/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject
import uk.gov.hmrc.apinotificationpull.connectors.ApiNotificationQueueConnector
import uk.gov.hmrc.apinotificationpull.model.{Notification, Notifications}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ApiNotificationQueueService @Inject()(apiNotificationQueueConnector: ApiNotificationQueueConnector)(implicit ec: ExecutionContext) {

  def getNotifications()(implicit hc: HeaderCarrier): Future[Notifications] = {
    apiNotificationQueueConnector.getNotifications()
  }

  def getAndRemoveNotification(notificationId: String)(implicit hc: HeaderCarrier): Future[Option[Notification]] = {

    for {
      notification <- apiNotificationQueueConnector.getById(notificationId)
      _ <- removeFromQueue(notification)
    } yield notification
  }

  private def removeFromQueue(notification: Option[Notification])(implicit hc: HeaderCarrier): Future[Unit] = {
    Future(notification.foreach(n => apiNotificationQueueConnector.delete(n)))
  }
}
