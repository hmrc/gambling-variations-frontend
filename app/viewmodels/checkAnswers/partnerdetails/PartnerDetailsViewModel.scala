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

package viewmodels.checkAnswers.partnerdetails

import config.FrontendAppConfig
import controllers.partnerdetails.routes
import models.{CheckMode, NormalMode, UserAnswers}
import pages.BusinessNumberOrIndex
import pages.partnerdetails.*
import play.api.i18n.Messages
import utils.PartnerUtils

import scala.collection.Seq
import java.time.LocalDate
import java.time.format.DateTimeFormatter

final case class PartnerDetailsViewModel(
  partners: Seq[PartnerDetailsRow],
  addAnotherPartner: Boolean,
  showNoPartnersMessage: Boolean,
  showMinimumPartnersMessage: Boolean,
  showMaximumPartnersMessage: Boolean,
  showSubmitMessage: Boolean
)

final case class PartnerDetailsRow(
  index: BusinessNumberOrIndex,
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
    partnerNumbers: Seq[BusinessNumberOrIndex],
    todayDate: LocalDate,
    userAnswers: UserAnswers,
    frontendAppConfig: FrontendAppConfig
  )(implicit messages: Messages): PartnerDetailsViewModel = {
    val activePartnerCount =
      partnerNumbers.count { partnerNumber =>

        val dateOfLeaving =
          userAnswers.get(
            PartnerDetailsDateOfLeavingPage(partnerNumber)
          )

        dateOfLeaving match {
          case Some(leavingDate) if !leavingDate.isBefore(todayDate) =>
            false
          case _ =>
            true
        }
      }

    val rows: Seq[PartnerDetailsRow] =
      partnerNumbers
        .flatMap { partnerNumber =>
          userAnswers
            .get(PartnerDetailsMgdRegNumberPage(partnerNumber))
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

              val status =
                dateOfLeaving match {
                  case Some(leavingDate) if !leavingDate.isBefore(todayDate) =>
                    messages("partnerDetails.status.dueToLeave")

                  case _ =>
                    dateOfJoining match {
                      case Some(joiningDate) if !joiningDate.isBefore(todayDate) =>
                        messages("partnerDetails.status.dueToJoin")

                      case _ =>
                        messages("partnerDetails.status.active")
                    }
                }

              val statusDetails =
                dateOfLeaving match {
                  case Some(leavingDate) if !leavingDate.isBefore(todayDate) =>
                    Some(leavingDate.format(dateFormatter))

                  case _ =>
                    dateOfJoining match {
                      case Some(joiningDate) if !joiningDate.isBefore(todayDate) =>
                        Some(joiningDate.format(dateFormatter))

                      case _ =>
                        None
                    }
                }

              val canRemove =
                dateOfLeaving.isEmpty &&
                  activePartnerCount > 2

              val removeUrl =
                if (canRemove) {
                  Some(onRemoveRoute(partnerNumber))
                } else {
                  None
                }

              PartnerDetailsRow(
                index             = partnerNumber,
                name              = name,
                status            = status,
                statusDetails     = statusDetails,
                partnerDetailsUrl = onPartnerDetailsRoute(partnerNumber),
                removeUrl         = removeUrl,
                canRemove         = canRemove
              )
            }
        }
        .sortBy(_.name.toLowerCase)

    val hasPartners =
      rows.nonEmpty

    val canAddAnotherPartner =
      rows.size < frontendAppConfig.maxPartners

    PartnerDetailsViewModel(
      partners                   = rows,
      addAnotherPartner          = canAddAnotherPartner,
      showNoPartnersMessage      = !hasPartners,
      showMinimumPartnersMessage = hasPartners && activePartnerCount <= 2,
      showMaximumPartnersMessage = rows.size >= frontendAppConfig.maxPartners,
      showSubmitMessage          = userAnswers.get(PartnerDetailsChangedPage).contains(true)
    )

  }

  private def onPartnerDetailsRoute(partnerNumber: BusinessNumberOrIndex): String = partnerNumber match {
    case _: Int =>
      routes.PartnerDetailsController
        .onPartnerDetails(partnerNumber.toString, NormalMode)
        .url
    case _: String =>
      routes.PartnerDetailsController
        .onPartnerDetails(partnerNumber.toString, CheckMode)
        .url
  }

  private def onRemoveRoute(partnerNumber: BusinessNumberOrIndex): String = partnerNumber match {
    case _: Int =>
      routes.PartnerDetailsController
        .onRemove(partnerNumber.toString, NormalMode)
        .url
    case _: String =>
      routes.PartnerDetailsController
        .onRemove(partnerNumber.toString, CheckMode)
        .url
  }

}
