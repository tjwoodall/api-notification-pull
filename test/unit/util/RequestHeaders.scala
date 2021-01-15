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

package unit.util

import play.api.http.HeaderNames.ACCEPT

object RequestHeaders {

  val ACCEPT_HEADER_VALUE = "application/vnd.hmrc.1.0+xml"

  val X_CLIENT_ID_HEADER_NAME = "X-Client-ID"

  val ClientId = "client-id"

  lazy val ACCEPT_HEADER: (String, String) = ACCEPT -> ACCEPT_HEADER_VALUE

  lazy val X_CLIENT_ID_HEADER: (String, String) = X_CLIENT_ID_HEADER_NAME -> ClientId

  val LoggingHeaders: Seq[(String, String)] = Seq(X_CLIENT_ID_HEADER, ACCEPT_HEADER)

}