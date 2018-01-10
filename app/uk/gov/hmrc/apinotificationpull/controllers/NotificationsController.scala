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

package uk.gov.hmrc.apinotificationpull.controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.apinotificationpull.model.XmlErrorResponse
import uk.gov.hmrc.apinotificationpull.presenters.NotificationPresenter
import uk.gov.hmrc.apinotificationpull.services.ApiNotificationQueueService
import uk.gov.hmrc.apinotificationpull.util.XmlBuilder
import uk.gov.hmrc.apinotificationpull.validators.HeaderValidator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class NotificationsController @Inject()(apiNotificationQueueService: ApiNotificationQueueService,
                                        headerValidator: HeaderValidator,
                                        notificationPresenter: NotificationPresenter,
                                        xmlBuilder: XmlBuilder) extends BaseController {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val X_CLIENT_ID_HEADER_NAME = "X-Client-ID"

  private def recovery: PartialFunction[Throwable, Result] = {
    case e =>
      Logger.error(s"An unexpected error occurred: ${e.getMessage}", e)
      InternalServerError(XmlErrorResponse("An unexpected error occurred"))
  }

  def delete(notificationId: String): Action[AnyContent] =
    (headerValidator.validateAcceptHeader andThen headerValidator.validateXClientIdHeader).async { implicit request =>

    implicit val hc: HeaderCarrier = buildHeaderCarrier(request)

    apiNotificationQueueService.getAndRemoveNotification(notificationId)
      .map(n => notificationPresenter.present(n))
  }

  def getAll: Action[AnyContent] =
    (headerValidator.validateAcceptHeader andThen headerValidator.validateXClientIdHeader).async { implicit request =>

      apiNotificationQueueService.getNotifications()(buildHeaderCarrier(request)).map {
        notifications => Ok(xmlBuilder.toXml(notifications)).as(XML)
      } recover recovery
  }

  private def buildHeaderCarrier(request: Request[AnyContent] ): HeaderCarrier = {
    request.headers.get(X_CLIENT_ID_HEADER_NAME) match {
      case Some(clientId: String) => hc.withExtraHeaders(X_CLIENT_ID_HEADER_NAME -> clientId)
      case _ =>
        // It should never happen
        Logger.warn(s"Header $X_CLIENT_ID_HEADER_NAME not found in the request.")
        hc
    }
  }
}
