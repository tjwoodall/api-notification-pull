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

import java.util.UUID

import javax.inject.Inject
import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier
import uk.gov.hmrc.apinotificationpull.logging.NotificationLogger
import uk.gov.hmrc.apinotificationpull.model.{Notification, NotificationStatus, Notifications}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException, _}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpClient


import scala.concurrent.{ExecutionContext, Future}

class EnhancedApiNotificationQueueConnector @Inject()(config: ServicesConfig, http: HttpClient, logger: NotificationLogger)(implicit ec: ExecutionContext) {

  private lazy val serviceBaseUrl: String = config.baseUrl("api-notification-queue")

  def getAllNotificationsBy(conversationId: UUID)(implicit hc: HeaderCarrier): Future[Notifications] = {

    val url = s"$serviceBaseUrl/notifications/conversationId/$conversationId"
    logger.debug(s"Calling get all notifications by using url: $url")
    http.GET[Notifications](url)
  }

  def getAllNotificationsBy(conversationId: UUID, notificationStatus: NotificationStatus.Value)(implicit hc: HeaderCarrier): Future[Notifications] = {

    val url = s"$serviceBaseUrl/notifications/conversationId/$conversationId/${notificationStatus.toString}"
    logger.debug(s"Calling get all notifications by using url: $url")
    http.GET[Notifications](url)
  }

  def getAllNotificationsBy(notificationStatus: NotificationStatus.Value)(implicit hc: HeaderCarrier): Future[Notifications] = {

    val url = s"$serviceBaseUrl/notifications/${notificationStatus.toString}"
    logger.debug(s"Calling get all notifications by using url: $url")
    http.GET[Notifications](url)
  }

  def getNotificationBy(notificationId: String, notificationStatus: NotificationStatus.Value)(implicit hc: HeaderCarrier): Future[Either[HttpException, Notification]] = {

    val url = s"$serviceBaseUrl/notifications/${notificationStatus.toString}/$notificationId"
    logger.debug(s"Calling get notifications by using url: $url")
    http.GET[HttpResponse](url)
      .map { r =>
        Right(Notification(notificationId, r.headers.map(h => h._1 -> h._2.head), r.body))
      }
      .recover[Either[HttpException, Notification]] {
      case nfe: NotFoundException => Left(nfe)
      case bre: BadRequestException => Left(bre)
      case ise => Left(new InternalServerException(ise.getMessage))
    }
  }
}
