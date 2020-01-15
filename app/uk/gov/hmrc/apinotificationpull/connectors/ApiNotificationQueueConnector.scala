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

package uk.gov.hmrc.apinotificationpull.connectors

import javax.inject.Inject
import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier
import uk.gov.hmrc.apinotificationpull.logging.NotificationLogger
import uk.gov.hmrc.apinotificationpull.model.{Notification, Notifications}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class ApiNotificationQueueConnector @Inject()(config: ServicesConfig, http: HttpClient, logger: NotificationLogger)(implicit ec: ExecutionContext) {

  private lazy val serviceBaseUrl: String = config.baseUrl("api-notification-queue")

  def getNotifications()(implicit hc: HeaderCarrier): Future[Notifications] = {
    val url = s"$serviceBaseUrl/notifications"
    logger.debug(s"Calling get notifications using url: $url")
    http.GET[Notifications](url)
  }

  def getById(notificationId: String)(implicit hc: HeaderCarrier): Future[Option[Notification]] = {

    val url = s"$serviceBaseUrl/notification/$notificationId"
    logger.debug(s"Getting notification by id using url: $url")
    http.GET[HttpResponse](url)
      .map { r =>
        logger.debug(s"Notification received successfully with id: $notificationId")
        Some(Notification(notificationId, r.allHeaders.map(h => h._1 -> h._2.head), r.body))
      }
      .recover[Option[Notification]] {
      case _: NotFoundException =>
        logger.debug(s"Notification not found with id: $notificationId")
        None
    }
  }

  def delete(notification: Notification)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = s"$serviceBaseUrl/notification/${notification.id}"
    logger.debug(s"Calling delete notifications using url: $url")
    http.DELETE(url)
  }

}
