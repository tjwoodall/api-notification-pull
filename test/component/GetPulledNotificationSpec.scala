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

import java.util.UUID

import org.scalatest.OptionValues._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import unit.util.XmlUtil.string2xml

import scala.xml.Utility.trim

class GetPulledNotificationSpec extends ComponentSpec with ExternalServices {

  override val clientId: String = UUID.randomUUID().toString

  override def beforeAll(): Unit = {
    startMockServer()
  }

  override protected def beforeEach() {
    resetMockServer()
  }

  override def afterAll(): Unit = {
    stopMockServer()
  }

  val notificationId = "notification-id"
  val validRequest = FakeRequest("GET", s"/pulled/$notificationId").
    withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> clientId)

  val validListRequest = FakeRequest("GET", s"/pulled").
    withHeaders(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> clientId)

  feature("GET a list of pulled notifications") {
    scenario("I want to successfully retrieve a list of pulled notifications") {

      Given("There is list of pulled notification in the API Notification Queue")

      val body = """{ "notifications": ["notification1", "notification2"] }""".stripMargin

      stubForExistingNotificationsList("/notifications/pulled", body,
        Seq(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> clientId))

      When("I call the GET pulled notifications endpoint")
      val result = route(app, validListRequest).value

      Then("I receive the list of notifications")
      status(result) shouldBe OK

      val expectedXml = scala.xml.Utility.trim(
        <resource href="/notifications/pulled/">
          <link rel="self" href="/notifications/pulled/"/>
          <link rel="notification" href="/notifications/pulled/notification1"/>
          <link rel="notification" href="/notifications/pulled/notification2"/>
        </resource>
      )

      string2xml(contentAsString(result)) shouldBe expectedXml
    }

    scenario("I want to successfully retrieve an empty list of notifications") {

      Given("There are no notification in the API Notification Queue")

      val body = """{ "notifications": [] }""".stripMargin

      stubForExistingNotificationsList("/notifications/pulled", body,
        Seq(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> clientId))

      When("I call the GET pulled notifications endpoint")
      val result = route(app, validListRequest).value

      Then("I receive the list of notifications")
      status(result) shouldBe OK

      val expectedXml = scala.xml.Utility.trim(
        <resource href="/notifications/pulled/">
          <link rel="self" href="/notifications/pulled/"/>
        </resource>
      )

      string2xml(contentAsString(result)) shouldBe expectedXml
    }
  }

  feature("GET a pulled notification by id") {

    scenario("I want to successfully retrieve a notification by notification id") {
      Given("There is a pulled notification in the API Notification Queue")

      val body = """<notification>some-notification</notification>""".stripMargin

      stubForExistingNotification("/notifications/pulled", notificationId, body,
        Seq(ACCEPT -> "application/vnd.hmrc.1.0+xml", xClientIdHeader -> clientId))

      When("I call the GET pulled notification endpoint")
      val result = route(app, validRequest).value

      Then("I receive the notification")
      status(result) shouldBe OK

      contentAsString(result).stripMargin shouldBe "<notification>some-notification</notification>"

    }

    scenario("I try to GET a pulled notification") {
      Given("A notification that is unpulled or doesn't exist")

      When("I call the GET pulled notification endpoint")
      val result = route(app, validRequest).value

      Then("I receive a NOT FOUND error")
      status(result) shouldBe NOT_FOUND

      val expectedXml = trim(<errorResponse>
        <code>NOT_FOUND</code>
        <message>Resource was not found</message>
        </errorResponse>
      )

      string2xml(contentAsString(result)) shouldBe expectedXml
    }
  }
}
