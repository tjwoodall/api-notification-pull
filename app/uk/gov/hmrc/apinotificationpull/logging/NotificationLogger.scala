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

package uk.gov.hmrc.apinotificationpull.logging

import uk.gov.hmrc.apinotificationpull.logging.LoggingHelper.formatWithHeaders
import uk.gov.hmrc.apinotificationpull.model.SeqOfHeader
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class NotificationLogger @Inject()(serviceConfig: ServicesConfig) {

  private lazy val loggerName: String = serviceConfig.getString("application.logger.name")
  lazy val logger = play.api.Logger(loggerName)

  def debug(msg: => String)(implicit headers: SeqOfHeader): Unit = logger.debug(formatWithHeaders(msg, headers))
  def info(msg: => String)(implicit headers: SeqOfHeader): Unit = logger.info(formatWithHeaders(msg, headers))
  def info(msg: => String, e: => Throwable)(implicit headers: SeqOfHeader): Unit = logger.info(formatWithHeaders(msg, headers), e)
  def warn(msg: => String)(implicit headers: SeqOfHeader): Unit = logger.warn(formatWithHeaders(msg, headers))
  def error(msg: => String)(implicit headers: SeqOfHeader): Unit = logger.error(formatWithHeaders(msg, headers))
  def error(msg: => String, e: => Throwable)(implicit headers: SeqOfHeader): Unit = logger.error(formatWithHeaders(msg, headers), e)

}
