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

package unit.validators

import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.apinotificationpull.validators.HeaderValidator
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.test.UnitSpec
import unit.util.RequestHeaders.{ACCEPT_HEADER, ACCEPT_HEADER_VALUE, X_CLIENT_ID_HEADER, X_CLIENT_ID_HEADER_NAME}
import unit.util.StubNotificationLogger

class HeaderValidatorSpec extends UnitSpec with MockitoSugar {

  private val stubLogger = new StubNotificationLogger(new CdsLogger(mock[ServicesConfig]))
  private val validator = new HeaderValidator(stubLogger, Helpers.stubControllerComponents())
  private val expectedResult = Ok("")

  private val validateAccept: Action[AnyContent] = validator.validateAcceptHeader {
    expectedResult
  }

  private val validateXClientId: Action[AnyContent] = validator.validateXClientIdHeader {
    expectedResult
  }

  "validateAcceptHeader" should {
    "return 406 NOT_ACCEPTABLE if Accept header is not present" in {
      status(validateAccept.apply(FakeRequest())) shouldBe NOT_ACCEPTABLE
    }

    "return 406 NOT_ACCEPTABLE if Accept header is invalid" in {
      status(validateAccept.apply(FakeRequest().withHeaders(ACCEPT -> "invalid"))) shouldBe NOT_ACCEPTABLE
    }

    s"return result from block if Accept header is $ACCEPT_HEADER_VALUE" in {
      await(validateAccept.apply(FakeRequest().withHeaders(ACCEPT_HEADER))) shouldBe expectedResult
    }
  }

  "validateXClientIdHeader" should {
    s"return 500 INTERNAL_SERVER_ERROR if $X_CLIENT_ID_HEADER_NAME header is not present" in {
      status(validateXClientId.apply(FakeRequest())) shouldBe INTERNAL_SERVER_ERROR

    }

    s"return result from block if $X_CLIENT_ID_HEADER_NAME header is present" in {
      await(validateXClientId.apply(FakeRequest().withHeaders(X_CLIENT_ID_HEADER))) shouldBe expectedResult
    }
  }
}
