/*
 * Copyright 2023 HM Revenue & Customs
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

package unit.logging

import uk.gov.hmrc.apinotificationpull.logging.LoggingHelper
import util.UnitSpec
import unit.util.RequestHeaders

class LoggingHelperSpec extends UnitSpec {

  val debugMsg = "DEBUG"

  "LoggingHelper" should {

    "format with headers" in {
      val actual = LoggingHelper.formatWithHeaders(debugMsg, RequestHeaders.LoggingHeaders)

      actual shouldBe "[clientId=client-id] DEBUG\nheaders=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml), (authorization,value-not-logged), (x-client-authorization-token,value-not-logged))"
    }

    "format without headers" in {
      val actual = LoggingHelper.formatWithHeaders(debugMsg, Seq.empty)

      actual shouldBe " DEBUG\nheaders=List()"
    }

  }
}
