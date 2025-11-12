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

package unit.controllers

import controllers.Assets
import org.apache.pekko.stream.Materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.http.{ContentTypes, MimeTypes}
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.mvc.{Codec, Result, Results}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.apinotificationpull.config.AppContext
import uk.gov.hmrc.apinotificationpull.controllers.dynamicControllers.ApiDocumentationController
import unit.util.MaterializerSupport
import util.UnitSpec
import views.txt

class DefinitionControllerSpec extends UnitSpec with MaterializerSupport with MockitoSugar {

  private implicit val mockMaterializer: Materializer = mock[Materializer]
  private val apiScope = "scope"
  private val apiContext = "context"
  private val appContext = new AppContext(Configuration("api.definition.scope" -> apiScope, "api.context" -> apiContext))
  private val mockAssets = mock[Assets]
  private val stubCc = Helpers.stubControllerComponents()
  private val controller = new ApiDocumentationController(mockAssets, stubCc, appContext)

  "DefinitionController.definition" should {
    lazy val result: Result = getDefinition(controller)

    "return OK status" in {
      status(result) shouldBe OK
    }

    "have a JSON content type" in {
      result.body.contentType shouldBe Some("application/json; charset=utf-8")
    }

    "return definition in the body" in {
      jsonBodyOf(result) shouldBe Json.parse(txt.definition(apiContext).toString())
    }
  }

  "DefinitionController.conf" should {
    lazy val result: Result = getConf(controller, "version", "file.conf")

    when(mockAssets.at(any, any, any)).thenReturn(
      stubCc.actionBuilder {
        Results.Ok.as(ContentTypes.withCharset(MimeTypes.JSON)(Codec.utf_8))
      }
    )

    "return OK status" in {
      status(result) shouldBe OK
    }

    "have a JSON content type" in {
      result.body.contentType shouldBe Some("application/json; charset=utf-8")
    }
  }

  private def getDefinition(controller: ApiDocumentationController) = {
    controller.definition().apply(FakeRequest("GET", "/api/definition")).futureValue
  }

  private def getConf(controller: ApiDocumentationController, version: String, file: String) = {
    controller.conf(version, file).apply(FakeRequest("GET", s"/api/conf/$version/$file ")).futureValue
  }
}
