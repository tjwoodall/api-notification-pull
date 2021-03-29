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

package uk.gov.hmrc.apinotificationpull.logging

import play.api.http.HeaderNames.AUTHORIZATION
import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.X_CLIENT_ID_HEADER_NAME
import uk.gov.hmrc.apinotificationpull.model.SeqOfHeader

object LoggingHelper {

  private val headerOverwriteValue = "value-not-logged"
  private val headersToOverwrite = Set(AUTHORIZATION.toLowerCase, "x-client-authorization-token")

  def formatWithHeaders(msg: String, headers: SeqOfHeader): String = {
    s"${formatLogPrefixWithClientId(headers)} $msg\nheaders=${overwriteHeaderValues(headers, headersToOverwrite)}"
  }

  private def formatLogPrefixWithClientId(headers: SeqOfHeader): String = {
    val maybeClientId = findHeaderValue(X_CLIENT_ID_HEADER_NAME, headers)

    maybeClientId.fold("")(clientId => s"[clientId=$clientId]")
  }

  private def findHeaderValue(headerName: String, headers: SeqOfHeader): Option[String] = {
    headers.collectFirst{
        case header if header._1.equalsIgnoreCase(headerName) => header._2
    }
  }

  private def overwriteHeaderValues(headers: SeqOfHeader, overwrittenHeaderNames: Set[String]): SeqOfHeader = {
    headers map {
      case (rewriteHeader, _) if overwrittenHeaderNames.contains(rewriteHeader) => rewriteHeader -> headerOverwriteValue
      case header => header
    }
  }
  
}
