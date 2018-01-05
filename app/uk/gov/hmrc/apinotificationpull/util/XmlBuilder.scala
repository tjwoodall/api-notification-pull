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

package uk.gov.hmrc.apinotificationpull.util

import uk.gov.hmrc.apinotificationpull.model.Notifications

object XmlBuilder {

  def toXml(notifications: Notifications): scala.xml.Elem = {

    def toXml(notification: String): scala.xml.Elem = {
      <notification>{notification}</notification>
    }

    <notifications>{notifications.notifications.map( n => toXml(n))}</notifications>
  }

}
