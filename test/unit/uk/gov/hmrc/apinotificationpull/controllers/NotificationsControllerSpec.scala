/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.apinotificationpull.controllers

import java.util.UUID

import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.test.FakeRequest
import uk.gov.hmrc.apinotificationpull.fakes.SuccessfulHeaderValidatorFake
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class NotificationsControllerSpec extends UnitSpec with WithFakeApplication {

  private val headerValidator = new SuccessfulHeaderValidatorFake
  private val controller = new NotificationsController(headerValidator)
  private val notificationId = UUID.randomUUID()
  private val xClientIdHeader = "X-Client-ID"

  "delete notification by id" when {
    val validRequest = FakeRequest("DELETE", s"/$notificationId").
      withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> "client-id")

    "notification does not exist" should {
      "return 404 NOT_FOUND response" in {
        val result = controller.delete(notificationId.toString).apply(validRequest)

        status(result) shouldBe NOT_FOUND
      }
    }
  }

}
