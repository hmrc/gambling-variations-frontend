/*
 * Copyright 2026 HM Revenue & Customs
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

package utils

import models.UserAnswers
import models.requests.DataRequest
import pages.QuestionPage
import play.api.libs.json.Format.GenericFormat
import repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

object FlagsUtil {
  def checkFlag(userAnswers: UserAnswers, changesPage: QuestionPage[Boolean], submittedOnlyPage: QuestionPage[Boolean]): Boolean = {
    val changed = userAnswers.get(changesPage).getOrElse(false)
    val submittedOnly = userAnswers.get(submittedOnlyPage).getOrElse(false)
    changed || submittedOnly
  }

  private def compareAnswers[A](userAnswers: UserAnswers,
                                sessionRepository: SessionRepository,
                                referencePage: QuestionPage[A],
                                changesPage: QuestionPage[Boolean]
                               ): Unit = {}

  def flagIfChanged(value: Any, sessionRepository: SessionRepository, referencePage: QuestionPage[UserAnswers], changesPage: QuestionPage[Boolean])(
    implicit
    request: DataRequest[?],
    ua: UserAnswers,
    ec: ExecutionContext
  ): Future[Boolean] = {
    sessionRepository.get(referencePage).map {
      case Some(savedValue) => savedValue != value
      case None => true
    }
  }
}
