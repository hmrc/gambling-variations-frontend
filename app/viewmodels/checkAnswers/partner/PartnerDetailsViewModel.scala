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

import config.FrontendAppConfig
import controllers.partner.routes
import models.UserAnswers
import pages.partner.PartnerDetailsChangedPage
import pages.partnerdetails.{PartnerDetailsBusinessNamePage, PartnerDetailsDateOfJoiningPage, PartnerDetailsDateOfLeavingPage, PartnerDetailsPage, PartnerDetailsTradingNamePage}
import play.api.i18n.Messages

import java.time.{LocalDate, ZoneOffset}
import java.time.format.DateTimeFormatter

final case class PartnerDetailsViewModel(
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
  removeUrl: Option[String],
  canRemove: Boolean
)

object PartnerDetailsViewModel {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

  def from(
    userAnswers: UserAnswers,
    frontendAppConfig: FrontendAppConfig
  )(implicit messages: Messages): PartnerDetailsViewModel = {
    val maxPartners = frontendAppConfig.maxPartners

    val today = LocalDate.now(ZoneOffset.UTC)

    val partnerNumbers: Seq[Int] =
      (0 until maxPartners).filter { partnerNumber =>
        userAnswers
          .get(PartnerDetailsPage(partnerNumber))
          .isDefined
      }

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

              val dateOfJoining =
                userAnswers.get(
                  PartnerDetailsDateOfJoiningPage(partnerNumber)
                )

              val dateOfLeaving =
                userAnswers.get(
                  PartnerDetailsDateOfLeavingPage(partnerNumber)
                )

              /*
               * Status logic:
               *
               * 1. Future leaving date -> Due to leave
               * 2. Future joining date -> Due to join
               * 3. Otherwise -> Active
               */
              val status =
                dateOfLeaving match {
                  case Some(leavingDate) if !leavingDate.isBefore(today) =>
                    messages("partnerDetails.status.dueToLeave")

                  case _ =>
                    dateOfJoining match {
                      case Some(joiningDate) if !joiningDate.isBefore(today) =>
                        messages("partnerDetails.status.dueToJoin")

                      case _ =>
                        messages("partnerDetails.status.active")
                    }
                }

              val statusDetails =
                dateOfLeaving match {
                  case Some(leavingDate) if leavingDate.isAfter(today) =>
                    Some(leavingDate.format(dateFormatter))

                  case _ =>
                    dateOfJoining match {
                      case Some(joiningDate) if joiningDate.isAfter(today) =>
                        Some(joiningDate.format(dateFormatter))

                      case _ =>
                        None
                    }
                }

              /*
               * Action logic:
               *
               * dateOfLeaving blank/null -> Remove
               * dateOfLeaving populated -> Cannot remove
               */
              val canRemove =
                dateOfLeaving.isEmpty

              val removeUrl =
                if (canRemove) {
                  Some(
                    routes.PartnerDetailsController
                      .onRemove(partnerNumber)
                      .url
                  )
                } else {
                  None
                }

              PartnerDetailsRow(
                partnerNumber = partnerNumber,
                name          = name,
                status        = status,
                statusDetails = statusDetails,
                partnerDetailsUrl = routes.PartnerDetailsController
                  .onPartnerDetails(partnerNumber)
                  .url,
                removeUrl = removeUrl,
                canRemove = canRemove
              )
            }
        }
        .sortBy(_.name.toLowerCase)

    val activePartnerCount =
      rows.count { row =>
        row.status == messages("partnerDetails.status.active")
      }

    val hasPartners =
      rows.nonEmpty

    val canAddAnotherPartner =
      rows.size < maxPartners

    PartnerDetailsViewModel(
      partners                   = rows,
      continueUrl                = routes.PartnerDetailsController.onContinue.url,
      addAnotherPartner          = canAddAnotherPartner,
      showNoPartnersMessage      = !hasPartners,
      showMinimumPartnersMessage = hasPartners && activePartnerCount < 3,
      showMaximumPartnersMessage = rows.size >= maxPartners,
      showSubmitMessage          = userAnswers.get(PartnerDetailsChangedPage()).contains(true)
    )
  }
}
