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

package component

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.{HttpHeader, HttpHeaders}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.test.Helpers.OK
import util.WireMockRunner

trait ExternalServices extends WireMockRunner {

  private val notificationId1 = 1234
  private val notificationId2 = 6789

  val clientId = "client-id"
  val xClientIdHeader = "X-Client-ID"

  def stubForAllNotifications(): StubMapping = {
    stubFor(get(urlMatching("/notifications"))
      .willReturn(aResponse()
        .withStatus(OK)
        .withBody(s"""{"notifications":["/notifications/$notificationId1","/notifications/$notificationId2"]}""")
      ))
  }

  def stubForExistingNotification(notificationId: String, notificationBody: String, headers: (String, String)*): StubMapping = {
    stubFor(get(urlMatching(s"/notification/$notificationId")).withHeader(xClientIdHeader, equalTo(clientId))
      .willReturn(aResponse()
        .withHeaders(new HttpHeaders(headers.map(h => HttpHeader.httpHeader(h._1, h._2)): _*))
        .withBody(notificationBody)
        .withStatus(OK)))

    stubFor(delete(urlMatching(s"/notification/$notificationId")).withHeader(xClientIdHeader, equalTo(clientId))
      .willReturn(aResponse()
        .withStatus(OK)))
  }

}
