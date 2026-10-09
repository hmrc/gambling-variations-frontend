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

package controllers.partnerdetails

import models.UserAnswers
import pages.BusinessNumberOrIndex
import pages.partnerdetails.PartnerDetailsHasClickedOnChangeOrRemovePage
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerChangeTracker @Inject() (sessionRepository: SessionRepository)(implicit ec: ExecutionContext) {

  /** Records that an existing partner's change/remove screen was opened. New partners are left untouched. */
  def markClicked(userAnswers: UserAnswers, index: BusinessNumberOrIndex): Future[UserAnswers] =
    index match {
      case businessNumber: String =>
        for {
          updated <- Future.fromTry(userAnswers.set(PartnerDetailsHasClickedOnChangeOrRemovePage(businessNumber), true))
          _       <- sessionRepository.set(updated)
        } yield updated
      case _: Int =>
        Future.successful(userAnswers)
    }
}
