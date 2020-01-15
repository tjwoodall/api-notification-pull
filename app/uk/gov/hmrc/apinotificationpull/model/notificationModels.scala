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

package uk.gov.hmrc.apinotificationpull.model

import play.api.libs.json.Json

object NotificationStatus extends Enumeration {
  val Unpulled = Value("unpulled")
  val Pulled = Value("pulled")
}

case class Notifications(notifications: List[String])

object Notifications {
  implicit val notificationsJF = Json.format[Notifications]
}

case class Notification(id: String, headers: Map[String, String], payload: String)

object XmlErrorResponse {
  def apply(message: String): String =
    <error_response>
      <code>UNKNOWN_ERROR</code>
      <errors>
        <error><type>SERVICE_UNAVAILABLE</type>
          <description>{message}</description>
        </error>
      </errors>
    </error_response>.toString()
}
