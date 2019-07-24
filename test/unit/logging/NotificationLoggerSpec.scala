/*
 * Copyright 2019 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.apinotificationpull.logging.NotificationLogger
import uk.gov.hmrc.customs.api.common.logging.CdsLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import unit.util.RequestHeaders.LoggingHeaders
import util.MockitoPassByNameHelper.PassByNameVerifier

class NotificationLoggerSpec extends UnitSpec with MockitoSugar {

  trait SetUp {
    val mockCdsLogger: CdsLogger = mock[CdsLogger]
    val logger = new NotificationLogger(mockCdsLogger)

    implicit val mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]
    when(mockHeaderCarrier.headers).thenReturn(LoggingHeaders)
  }

  "NotificationsLogger" should {
    "debug(s: => String)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      logger.debug("msg")

      PassByNameVerifier(mockCdsLogger, "debug")
        .withByNameParam("[clientId=client-id] msg\nheaders=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml))")
        .verify()
    }

    "info(s: => String, e: => Throwable)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      logger.info("msg", new Exception(""))

      PassByNameVerifier(mockCdsLogger, "info")
        .withByNameParam("[clientId=client-id] msg\nheaders=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml))")
        .withByNameParamMatcher(any[Throwable])
        .verify()
    }

    "info(s: => String)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      logger.info("msg")

      PassByNameVerifier(mockCdsLogger, "info")
        .withByNameParam("[clientId=client-id] msg\nheaders=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml))")
        .verify()
    }

    "warn(s: => String)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      logger.warn("msg")

      PassByNameVerifier(mockCdsLogger, "warn")
        .withByNameParam("[clientId=client-id] msg\nheaders=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml))")
        .verify()
    }

    "error(s: => String)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      logger.error("msg")

      PassByNameVerifier(mockCdsLogger, "error")
        .withByNameParam("[clientId=client-id] msg\nheaders=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml))")
        .verify()
    }

    "error(s: => String, e: => Throwable)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      logger.error("msg", new Exception(""))

      PassByNameVerifier(mockCdsLogger, "error")
        .withByNameParam("[clientId=client-id] msg\nheaders=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml))")
        .withByNameParamMatcher(any[Throwable])
        .verify()
    }

    "error(s: => String, e: => Throwable)(implicit headers: SeqOfHeader) for getting headers from request implicitly" in new SetUp {
      implicit val mockRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(LoggingHeaders: _*)
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromRequest

      logger.error("msg", new Exception(""))

      PassByNameVerifier(mockCdsLogger, "error")
        .withByNameParam("[clientId=client-id] msg\nheaders=List((Host,localhost), (X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml))")
        .withByNameParamMatcher(any[Throwable])
        .verify()
    }
  }
}
