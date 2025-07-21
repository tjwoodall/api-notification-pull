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

package uk.gov.hmrc.apinotificationpull.controllers.documentControllers

import controllers.Assets
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.apinotificationpull.config.AppContext
import uk.gov.hmrc.apinotificationpull.controllers.dynamicControllers.ApiDocumentationController

import javax.inject.{Inject, Singleton}

@Singleton
class DocumentationController @Inject()(assets: Assets, cc: ControllerComponents, appContext: AppContext)
  extends ApiDocumentationController(assets, cc, appContext) {

}
