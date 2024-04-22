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

package unit.util

import org.mockito.scalatest.MockitoSugar
import uk.gov.hmrc.apinotificationpull.config.AppContext
import uk.gov.hmrc.apinotificationpull.model.NotificationStatus.{Pulled, Unpulled}
import uk.gov.hmrc.apinotificationpull.model.Notifications
import uk.gov.hmrc.apinotificationpull.util.EnhancedXmlBuilder
import util.UnitSpec

class EnhancedXmlBuilderSpec extends UnitSpec with MockitoSugar {

  trait Setup {
    val apiContext = "notifications"
    val appContext = mock[AppContext]
    val xmlBuilder = new EnhancedXmlBuilder(appContext)
  }

  "XmlBuilder.toXml()" should {

    "convert pulled notifications to XML" in new Setup {
      val notifications = Notifications(List("/notifications/pulled/123", "/notifications/pulled/456"))

      val expectedXml = scala.xml.Utility.trim(
        <resource href="/notifications/pulled/">
          <link rel="self" href="/notifications/pulled/"/>
          <link rel="notification" href="/notifications/pulled/123"/>
          <link rel="notification" href="/notifications/pulled/456"/>
        </resource>
      )

       xmlBuilder.toXml(notifications, Pulled) shouldBe expectedXml
    }

    "convert empty pulled notifications to XML" in new Setup {
      val notifications = Notifications(Nil)

      val expectedXml = scala.xml.Utility.trim(
        <resource href="/notifications/pulled/">
          <link rel="self" href="/notifications/pulled/"/>
        </resource>
      )

      xmlBuilder.toXml(notifications, Pulled) shouldBe expectedXml
    }

    "convert unpulled notifications to XML" in new Setup {
      val notifications = Notifications(List("/notifications/unpulled/123", "/notifications/unpulled/456"))

      val expectedXml = scala.xml.Utility.trim(
        <resource href="/notifications/unpulled/">
          <link rel="self" href="/notifications/unpulled/"/>
          <link rel="notification" href="/notifications/unpulled/123"/>
          <link rel="notification" href="/notifications/unpulled/456"/>
        </resource>
      )

      xmlBuilder.toXml(notifications, Unpulled) shouldBe expectedXml
    }

    "convert empty unpulled notifications to XML" in new Setup {
      val notifications = Notifications(Nil)

      val expectedXml = scala.xml.Utility.trim(
        <resource href="/notifications/unpulled/">
          <link rel="self" href="/notifications/unpulled/"/>
        </resource>
      )

      xmlBuilder.toXml(notifications, Unpulled) shouldBe expectedXml
    }
  }
}
