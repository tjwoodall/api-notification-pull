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

package uk.gov.hmrc.apinotificationpull.controllers

import akka.util.ByteString
import javax.inject.{Inject, Singleton}
import play.api.http.HttpEntity
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import uk.gov.hmrc.apinotificationpull.controllers.CustomHeaderNames.{X_CLIENT_ID_HEADER_NAME, getHeadersFromRequest}
import uk.gov.hmrc.apinotificationpull.logging.NotificationLogger
import uk.gov.hmrc.apinotificationpull.model.NotificationStatus
import uk.gov.hmrc.apinotificationpull.model.NotificationStatus._
import uk.gov.hmrc.apinotificationpull.services.EnhancedApiNotificationQueueService
import uk.gov.hmrc.apinotificationpull.util.EnhancedXmlBuilder
import uk.gov.hmrc.apinotificationpull.validators.HeaderValidator
import uk.gov.hmrc.customs.api.common.controllers.ErrorResponse.{ErrorInternalServerError, ErrorNotFound, errorBadRequest}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.controller.BaseController

@Singleton
class EnhancedNotificationsController @Inject()(enhancedApiNotificationQueueService: EnhancedApiNotificationQueueService,
                                                headerValidator: HeaderValidator,
                                                enhancedXmlBuilder: EnhancedXmlBuilder,
                                                logger: NotificationLogger) extends BaseController {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val badRequestPulledText = "Notification is unpulled"
  private val badRequestUnpulledText = "Notification has been pulled"

  def pulled(notificationId: String): Action[AnyContent] = get(notificationId, Pulled, badRequestPulledText)

  def pulledList(): Action[AnyContent] = getList(Pulled)

  def unpulled(notificationId: String): Action[AnyContent] = get(notificationId, Unpulled, badRequestUnpulledText)

  def unpulledList(): Action[AnyContent] = getList(Unpulled)

  private def getList(notificationStatus: NotificationStatus.Value): Action[AnyContent] =
  (headerValidator.validateAcceptHeader andThen headerValidator.validateXClientIdHeader).async { implicit request =>

    logger.debug("In EnhancedNotificationsController.getList")

    implicit val hc: HeaderCarrier = buildHeaderCarrier()

    enhancedApiNotificationQueueService.getAllNotificationsBy(notificationStatus).map { notifications =>
      Ok(enhancedXmlBuilder.toXml(notifications, notificationStatus)).as(XML)
    } recover recovery
  }

  private def get(notificationId: String, notificationStatus: NotificationStatus.Value, badRequestText: String): Action[AnyContent] =
    (headerValidator.validateAcceptHeader andThen headerValidator.validateXClientIdHeader).async { implicit request =>
      logger.debug("In EnhancedNotificationsController.get")
      implicit val hc: HeaderCarrier = buildHeaderCarrier()
      enhancedApiNotificationQueueService.getNotificationBy(notificationId, notificationStatus)
        .map {
          case Right(n) => Result(
            header = ResponseHeader(OK),
            body = HttpEntity.Strict(ByteString(n.payload), n.headers.get(CONTENT_TYPE)))
            .withHeaders(n.headers.toSeq: _*)
          case Left(nfe: NotFoundException) =>
            logger.info(s"Notification not found for id: $notificationId", nfe)
            ErrorNotFound.XmlResult
          case Left(bre: BadRequestException) =>
            logger.info(s"$badRequestText for id: $notificationId", bre)
            errorBadRequest(badRequestText).XmlResult
          case Left(e) =>
            logger.error(s"Internal server error for notification id: $notificationId", e)
            ErrorInternalServerError.XmlResult
        }.recover(recovery(request))
  }

  private def buildHeaderCarrier()(implicit request: Request[AnyContent] ): HeaderCarrier = {
    val maybeClientId = request.headers.get(X_CLIENT_ID_HEADER_NAME)
    maybeClientId match {
      case Some(clientId: String) =>
        logger.debug(s"Got client id from header: $maybeClientId")
        hc.withExtraHeaders(X_CLIENT_ID_HEADER_NAME -> clientId)
      case _ =>
        // It should never happen
        logger.warn(s"Header $X_CLIENT_ID_HEADER_NAME not found in the request.")
        hc
    }
  }

  private def recovery[A](implicit request: Request[A]): PartialFunction[Throwable, Result] = {
    case e =>
      logger.error(s"An unexpected error occurred: ${e.getMessage}", e)
      ErrorInternalServerError.XmlResult
  }

}
