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

package viewmodels.checkAnswers.partner

import controllers.partner.routes
import models.UserAnswers
import pages.partnerdetails.{PartnerDetailsBusinessNamePage, PartnerDetailsPage, PartnerDetailsTradingNamePage}
import play.api.i18n.Messages

final case class PartnerDetailsViewModel(
  caption: String,
  heading: String,
  partners: Seq[PartnerDetailsRow],
  continueUrl: String,
  addAnotherPartner: Boolean,
  showNoPartnersMessage: Boolean,
  showMinimumPartnersMessage: Boolean,
  showMaximumPartnersMessage: Boolean,
  showSubmitMessage: Boolean
)

final case class PartnerDetailsRow(
  partnerNumber: Int,
  name: String,
  status: String,
  statusDetails: Option[String],
  partnerDetailsUrl: String,
  removeUrl: Option[String]
)

object PartnerDetailsViewModel {

  private val maxPartners = 100
  private val minimumActivePartners = 2

  def from(
    userAnswers: UserAnswers
  )(implicit messages: Messages): PartnerDetailsViewModel = {

    val partnerNumbers: Seq[Int] =
      (0 until maxPartners).filter { partnerNumber =>
        userAnswers
          .get(PartnerDetailsPage(partnerNumber))
          .isDefined
      }

    val activeStatus =
      messages("partnerDetails.status.active")

    val rows: Seq[PartnerDetailsRow] =
      partnerNumbers
        .flatMap { partnerNumber =>
          userAnswers
            .get(PartnerDetailsPage(partnerNumber))
            .map { mgdRegNumber =>

              val name =
                userAnswers
                  .get(PartnerDetailsTradingNamePage(partnerNumber))
                  .orElse(
                    userAnswers.get(
                      PartnerDetailsBusinessNamePage(partnerNumber)
                    )
                  )
                  .getOrElse(mgdRegNumber)

              PartnerDetailsRow(
                partnerNumber = partnerNumber,
                name          = name,
                status        = activeStatus,
                statusDetails = None,
                partnerDetailsUrl = routes.PartnerDetailsController
                  .onPartnerDetails(partnerNumber)
                  .url,
                removeUrl = None
              )
            }
        }
        .sortBy(_.name.toLowerCase)

    val activePartnerCount =
      rows.count(_.status == activeStatus)

    val canRemovePartner =
      activePartnerCount > minimumActivePartners

    val rowsWithRemoveAction =
      rows.map { row =>
        row.copy(
          removeUrl = if (canRemovePartner) {
            Some(
              routes.PartnerDetailsController
                .onRemove(row.partnerNumber)
                .url
            )
          } else {
            None
          }
        )
      }

    val hasPartners =
      rowsWithRemoveAction.nonEmpty

    val canAddAnotherPartner =
      rowsWithRemoveAction.size < maxPartners

    PartnerDetailsViewModel(
      caption                    = messages("changeRegistrationDetails.caption"),
      heading                    = messages("partnerDetails.heading"),
      partners                   = rowsWithRemoveAction,
      continueUrl                = routes.PartnerDetailsController.onContinue.url,
      addAnotherPartner          = canAddAnotherPartner,
      showNoPartnersMessage      = !hasPartners,
      showMinimumPartnersMessage = hasPartners && activePartnerCount < 3,
      showMaximumPartnersMessage = rowsWithRemoveAction.size >= maxPartners,
      showSubmitMessage          = hasPartners
    )
  }
}
