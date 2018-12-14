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

package uk.gov.hmrc.apinotificationpull.connectors

import javax.inject.Inject
import uk.gov.hmrc.apinotificationpull.model.NotificationStatus
import uk.gov.hmrc.apinotificationpull.config.ServiceConfiguration
import uk.gov.hmrc.apinotificationpull.model.Notification
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

class EnhancedApiNotificationQueueConnector @Inject()(config: ServiceConfiguration, http: HttpClient) {

  private lazy val serviceBaseUrl: String = config.baseUrl("api-notification-queue")

  def getNotificationBy(notificationId: String, notificationStatus: NotificationStatus.Value)(implicit hc: HeaderCarrier): Future[Either[HttpException, Notification]] = {

    http.GET[HttpResponse](s"$serviceBaseUrl/notifications/${notificationStatus.toString}/$notificationId")
      .map { r =>
        Right(Notification(notificationId, r.allHeaders.map(h => h._1 -> h._2.head), r.body))
      }
      .recover {
        case nfe: NotFoundException => Left(nfe)
        case bre: BadRequestException => Left(bre)
        case ise => Left(new InternalServerException(ise.getMessage))
      }

  }

}
