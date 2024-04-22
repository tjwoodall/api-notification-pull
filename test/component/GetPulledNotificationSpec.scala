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

package component

import org.scalatest.OptionValues._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import unit.util.RequestHeaders.{ACCEPT_HEADER, X_CLIENT_ID_HEADER}
import unit.util.XmlUtil.string2xml

import java.util.UUID
import scala.xml.Utility.trim

class GetPulledNotificationSpec extends ComponentSpec with ExternalServices {

  val clientId: String = UUID.randomUUID().toString

  override def beforeAll(): Unit = {
    startMockServer()
  }

  override protected def beforeEach(): Unit = {
    resetMockServer()
  }

  override def afterAll(): Unit = {
    stopMockServer()
  }

  val notificationId = "notification-id"
  val validRequest = FakeRequest("GET", s"/pulled/$notificationId").
    withHeaders(ACCEPT_HEADER, X_CLIENT_ID_HEADER)

  val validListRequest = FakeRequest("GET", s"/pulled").
    withHeaders(ACCEPT_HEADER, X_CLIENT_ID_HEADER)

  Feature("GET a list of pulled notifications") {
    Scenario("I want to successfully retrieve a list of pulled notifications") {

      Given("There is list of pulled notification in the API Notification Queue")

      val body = """{ "notifications": ["/notifications/pulled/notification1", "/notifications/pulled/notification2"] }""".stripMargin

      stubForExistingNotificationsList("/notifications/pulled", body,
        Seq(ACCEPT_HEADER, X_CLIENT_ID_HEADER))

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

    Scenario("I want to successfully retrieve an empty list of notifications") {

      Given("There are no notification in the API Notification Queue")

      val body = """{ "notifications": [] }""".stripMargin

      stubForExistingNotificationsList("/notifications/pulled", body,
        Seq(ACCEPT_HEADER, X_CLIENT_ID_HEADER))

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

  Feature("GET a pulled notification by id") {

    Scenario("I want to successfully retrieve a notification by notification id") {
      Given("There is a pulled notification in the API Notification Queue")

      val body = """<notification>some-notification</notification>""".stripMargin

      stubForExistingNotification("/notifications/pulled", notificationId, body,
        Seq(ACCEPT_HEADER, X_CLIENT_ID_HEADER))

      When("I call the GET pulled notification endpoint")
      val result = route(app, validRequest).value

      Then("I receive the notification")
      status(result) shouldBe OK

      contentAsString(result).stripMargin shouldBe "<notification>some-notification</notification>"

    }

    Scenario("I try to GET a pulled notification") {
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
