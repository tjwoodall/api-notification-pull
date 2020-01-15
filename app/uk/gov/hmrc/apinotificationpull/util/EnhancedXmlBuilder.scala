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

package uk.gov.hmrc.apinotificationpull.util

import java.util.UUID

import javax.inject.Inject
import uk.gov.hmrc.apinotificationpull.config.AppContext
import uk.gov.hmrc.apinotificationpull.model.{NotificationStatus, Notifications}

class EnhancedXmlBuilder @Inject()(appContext: AppContext) {

  private def toXml(notificationLocation: String): scala.xml.Elem =
      <link rel="notification" href={s"$notificationLocation"}/>

  def toXml(notifications: Notifications, notificationStatus: NotificationStatus.Value): scala.xml.Elem =
    <resource href={s"/notifications/$notificationStatus/"}><link rel="self" href={s"/notifications/$notificationStatus/"}/>{notifications.notifications.map(notificationLocation => toXml(notificationLocation))}</resource>

  def toXml(notifications: Notifications, conversationId: UUID): scala.xml.Elem =
    <resource href={s"/notifications/conversationId/$conversationId/"}><link rel="self" href={s"/notifications/conversationId/$conversationId/"}/>{notifications.notifications.map(notificationLocation => toXml(notificationLocation))}</resource>

  def toXml(notifications: Notifications, conversationId: UUID, notificationStatus: NotificationStatus.Value): scala.xml.Elem =
    <resource href={s"/notifications/conversationId/$conversationId/$notificationStatus/"}><link rel="self" href={s"/notifications/conversationId/$conversationId/$notificationStatus/"}/>{notifications.notifications.map(notificationLocation => toXml(notificationLocation))}</resource>
}
