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

package unit.presenters

import org.apache.pekko.stream.Materializer
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.ContentTypes.XML
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.Status._
import uk.gov.hmrc.apinotificationpull.model.Notification
import uk.gov.hmrc.apinotificationpull.presenters.NotificationPresenter
import unit.util.MaterializerSupport
import util.UnitSpec

class NotificationPresenterSpec extends UnitSpec with MaterializerSupport {

  private implicit val mockMaterializer: Materializer = mock[Materializer]

   "present" when {
     "no notification" should {
       trait PresentNoNotification {
         val presenter = new NotificationPresenter
         val result = presenter.present(None)
       }

       "return NOT_FOUND" in new PresentNoNotification {
         status(result) shouldBe NOT_FOUND
       }

       "return empty body" in new PresentNoNotification {
         bodyOf(result) shouldBe "NOT FOUND"
       }
     }

     "notificationExists" should {
       trait PresentSomeNotification {
         val presenter = new NotificationPresenter
         val notificationId = "notificationId"

         val header1: (String, String) = "Header1-Name" -> "Header1-value"
         val header2: (String, String) = "Header2-Name" -> "Header2-value"

         val notification = Notification(notificationId, Map(CONTENT_TYPE -> XML, header1, header2), "Notification")

         val result = presenter.present(Some(notification))
       }

       "return OK" in new PresentSomeNotification {
         status(result) shouldBe OK
       }

       "return notification payload in body" in new PresentSomeNotification {
         bodyOf(result) shouldBe notification.payload
       }

       "return headers" in new PresentSomeNotification {
         result.header.headers should contain allOf(header1, header2)
       }
     }
   }
}
