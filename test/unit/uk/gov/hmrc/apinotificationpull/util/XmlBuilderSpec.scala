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

package uk.gov.hmrc.apinotificationpull.util

import uk.gov.hmrc.apinotificationpull.model.Notifications
import uk.gov.hmrc.apinotificationpull.util.XmlBuilder.toXml
import uk.gov.hmrc.play.test.UnitSpec

class XmlBuilderSpec extends UnitSpec {

  "XmlBuilder.toXml()" should {

    "convert notifications to XML" in {
      val notifications = Notifications(List("/notifications/123", "/notifications/456"))

      val expectedXml = scala.xml.Utility.trim(
        <resource href="/notifications/">
          <link rel="self" href="/notifications/"/>
          <link rel="notification" href="/notifications/123"/>
          <link rel="notification" href="/notifications/456"/>
        </resource>
      )

       toXml(notifications) shouldBe expectedXml
    }

    "convert empty notifications to XML" in {
      val notifications = Notifications(Nil)

      val expectedXml = scala.xml.Utility.trim(
        <resource href="/notifications/">
          <link rel="self" href="/notifications/"/>
        </resource>
      )

      toXml(notifications) shouldBe expectedXml
    }

  }

}
