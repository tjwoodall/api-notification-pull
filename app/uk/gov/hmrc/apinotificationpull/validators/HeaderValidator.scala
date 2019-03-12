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

package uk.gov.hmrc.apinotificationpull.validators

import com.google.inject.Inject
import play.api.http.HeaderNames._
import play.api.http.Status._
import play.api.mvc.{ActionBuilder, Request, Result, Results}
import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.{ACCEPT_HEADER_VALUE, X_CLIENT_ID_HEADER_NAME, getHeadersFromRequest}
import uk.gov.hmrc.apinotificationpull.logging.NotificationLogger

import scala.concurrent.Future

class HeaderValidator @Inject()(logger: NotificationLogger) extends Results {

  private def validateHeader(rules: Option[String] => Boolean, headerName: String, error: Result): ActionBuilder[Request] =
    new ActionBuilder[Request] {
      override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
        implicit val implicitRequest: Request[A] = request
        val maybeHeader = request.headers.get(headerName)
        if (rules(maybeHeader)) {
          logger.info(s"$headerName passed validation: $maybeHeader")
          block(request)
        } else {
          logger.info(s"$headerName failed validation: $maybeHeader")
          Future.successful(error)
        }
      }
  }

  private val acceptHeaderRules: Option[String] => Boolean = _ contains ACCEPT_HEADER_VALUE
  private val xClientIdHeaderRules: Option[String] => Boolean = _ exists (_ => true)

  def validateAcceptHeader: ActionBuilder[Request] = validateHeader(acceptHeaderRules, ACCEPT, Status(NOT_ACCEPTABLE))
  def validateXClientIdHeader: ActionBuilder[Request] = validateHeader(xClientIdHeaderRules, X_CLIENT_ID_HEADER_NAME, Status(INTERNAL_SERVER_ERROR))
}
