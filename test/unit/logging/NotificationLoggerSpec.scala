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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.apinotificationpull.logging.NotificationLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.util.RequestHeaders.LoggingHeaders
import util.UnitSpec

import java.io.{ByteArrayOutputStream, PrintStream}
import scala.io.Source

class NotificationLoggerSpec extends UnitSpec with MockitoSugar {

  trait SetUp {
    private val mockServiceConfig = mock[ServicesConfig]
    val cdsLoggerName = "api-notification-pull"
    val logger = new NotificationLogger(mockServiceConfig)

    implicit val mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]
    when(mockHeaderCarrier.headers(any())).thenReturn(LoggingHeaders)
    when(mockHeaderCarrier.extraHeaders).thenReturn(Seq.empty)
    when(mockServiceConfig.getString(any[String])).thenReturn(cdsLoggerName)

    protected def captureLogging(blockWithLogging: => Any): List[String] = {
      import ch.qos.logback.classic.{BasicConfigurator, LoggerContext}
      import org.slf4j.LoggerFactory

      LoggerFactory.getILoggerFactory match {
        case lc: LoggerContext =>
          lc.reset()
          new BasicConfigurator().configure(lc)

        case unsupported => fail(s"Unexpected LoggerFactory configured in SLF4J: ${unsupported}")
      }

      val baos = new ByteArrayOutputStream()
      val stdout = System.out
      System.setOut(new PrintStream(baos))

      try blockWithLogging
      finally System.setOut(stdout)
      Source.fromBytes(baos.toByteArray).getLines().toList
    }
  }

  "NotificationsLogger" should {
    "debug(s: => String)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      val output: List[String] = captureLogging {
        logger.debug("msg")
      }

      output should have size 2
      output match {
        case loggedMessage :: loggedHeaders :: Nil =>
          loggedMessage should endWith(s"DEBUG $cdsLoggerName -- [clientId=client-id] msg")
          loggedHeaders shouldBe "headers=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml), (authorization,value-not-logged), (x-client-authorization-token,value-not-logged))"
        case _ => assert(false)
      }
    }

    "info(s: => String, e: => Throwable)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      val exception = new Exception("info")

      val output: List[String] = captureLogging {
        logger.info("msg", exception)
      }

      output.size should be > 3
      output match {
        case loggedMessage :: loggedHeaders :: exceptionMessage :: stacktrace =>
          loggedMessage should endWith(s"INFO $cdsLoggerName -- [clientId=client-id] msg")
          loggedHeaders shouldBe "headers=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml), (authorization,value-not-logged), (x-client-authorization-token,value-not-logged))"
          exceptionMessage shouldBe exception.toString
          stacktrace foreach (_ should startWith("\tat "))
        case _ => assert(false)
      }
    }

    "info(s: => String)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      val output: List[String] = captureLogging {
        logger.info("msg")
      }

      output should have size 2
      output match {
        case loggedMessage :: loggedHeaders :: Nil =>
          loggedMessage should endWith(s"INFO $cdsLoggerName -- [clientId=client-id] msg")
          loggedHeaders shouldBe "headers=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml), (authorization,value-not-logged), (x-client-authorization-token,value-not-logged))"
        case _ => assert(false)
      }
    }

    "warn(s: => String)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      val output: List[String] = captureLogging {
        logger.warn("msg")
      }

      output should have size 2
      output match {
        case loggedMessage :: loggedHeaders :: Nil =>
          loggedMessage should endWith(s"WARN $cdsLoggerName -- [clientId=client-id] msg")
          loggedHeaders shouldBe "headers=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml), (authorization,value-not-logged), (x-client-authorization-token,value-not-logged))"
        case _ => assert(false)
      }
    }

    "error(s: => String)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      val output: List[String] = captureLogging {
        logger.error("msg")
      }

      output should have size 2
      output match {
        case loggedMessage :: loggedHeaders :: Nil =>
          loggedMessage should endWith (s"ERROR $cdsLoggerName -- [clientId=client-id] msg")
          loggedHeaders shouldBe "headers=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml), (authorization,value-not-logged), (x-client-authorization-token,value-not-logged))"
        case _ => assert(false)
      }
    }

    "error(s: => String, e: => Throwable)(implicit headers: SeqOfHeader)" in new SetUp {
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromHeaderCarrier

      val exception = new Exception("error")

      val output: List[String] = captureLogging {
        logger.error("msg", exception)
      }

      output.size should be > 3
      output match {
        case loggedMessage :: loggedHeaders :: exceptionMessage :: stacktrace =>
          loggedMessage should endWith(s"ERROR $cdsLoggerName -- [clientId=client-id] msg")
          loggedHeaders shouldBe "headers=List((X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml), (authorization,value-not-logged), (x-client-authorization-token,value-not-logged))"
          exceptionMessage shouldBe exception.toString
          stacktrace foreach (_ should startWith("\tat "))
        case _ => assert(false)
      }
    }

    "error(s: => String, e: => Throwable)(implicit headers: SeqOfHeader) for getting headers from request implicitly" in new SetUp {
      implicit val mockRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(LoggingHeaders: _*)
      import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.getHeadersFromRequest

      val exception = new Exception("error")

      val output: List[String] = captureLogging {
        logger.error("msg", exception)
      }

      output.size should be > 3
      output match {
        case loggedMessage :: loggedHeaders :: exceptionMessage :: stacktrace =>
          loggedMessage should endWith(s"ERROR $cdsLoggerName -- [clientId=client-id] msg")
          loggedHeaders shouldBe "headers=List((Host,localhost), (X-Client-ID,client-id), (Accept,application/vnd.hmrc.1.0+xml), (authorization,value-not-logged), (x-client-authorization-token,value-not-logged))"
          exceptionMessage shouldBe exception.toString
          stacktrace foreach (_ should startWith("\tat "))
        case _ => assert(false)
      }
    }
  }
}
