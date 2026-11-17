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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.collection.Seq

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

  private val dateFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy")

  def from(
    paginatedPartnerNumbers: Seq[BusinessNumberOrIndex],
    allPartnerNumbers: Seq[BusinessNumberOrIndex],
    todayDate: LocalDate,
    userAnswers: UserAnswers,
    frontendAppConfig: FrontendAppConfig
  )(implicit messages: Messages): PartnerDetailsViewModel = {

    /*
     * This must use ALL partners, not just the current page.
     *
     * For example:
     *   100 total partners
     *   10 partners per page
     *
     * paginatedPartnerNumbers.size = 10
     * allPartnerNumbers.size       = 100
     */
    val activePartnerCount =
      allPartnerNumbers.count { partnerNumber =>

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

    /*
     * Only create rows for the current page.
     */
    val rows: Seq[PartnerDetailsRow] =
      paginatedPartnerNumbers
        .flatMap { partnerNumber =>
          for {
            _ <-
              userAnswers.get(
                PartnerDetailsMgdRegNumberPage(partnerNumber)
              )

            name <-
              PartnerUtils.getPartnerDetailsName(
                partnerNumber,
                userAnswers
              )

          } yield {

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

                case Some(_) =>
                  messages(
                    "partnerDetails.status.dueToLeave"
                  )

                case None =>
                  dateOfJoining match {

                    case Some(joiningDate) if !joiningDate.isBefore(todayDate) =>
                      messages(
                        "partnerDetails.status.dueToJoin"
                      )

                    case Some(joiningDate) =>
                      messages(
                        "partnerDetails.status.active",
                        joiningDate.format(dateFormatter)
                      )

                    case None =>
                      messages(
                        "partnerDetails.status.active"
                      )
                  }
              }

            val statusDetails =
              dateOfLeaving match {

                case Some(leavingDate) =>
                  Some(
                    leavingDate.format(dateFormatter)
                  )

                case None =>
                  dateOfJoining match {

                    case Some(joiningDate) if !joiningDate.isBefore(todayDate) =>
                      Some(
                        joiningDate.format(dateFormatter)
                      )

                    case _ =>
                      None
                  }
              }

            /*
             * activePartnerCount is based on ALL partners.
             *
             * Therefore the remove option is not incorrectly affected
             * by only having 10 partners on the current page.
             */
            val canRemove =
              dateOfLeaving.isEmpty &&
                activePartnerCount > 2

            val removeUrl =
              if (canRemove) {
                Some(
                  onRemoveRoute(partnerNumber)
                )
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

    /*
     * Use the complete collection for page-level business rules.
     */
    val totalPartnerCount =
      allPartnerNumbers.size

    val hasPartners =
      totalPartnerCount > 0

    val canAddAnotherPartner =
      totalPartnerCount < frontendAppConfig.maxPartners

    PartnerDetailsViewModel(
      partners              = rows,
      addAnotherPartner     = canAddAnotherPartner,
      showNoPartnersMessage = !hasPartners,
      showMinimumPartnersMessage = hasPartners &&
        activePartnerCount <= 2,
      showMaximumPartnersMessage = totalPartnerCount >= frontendAppConfig.maxPartners,
      showSubmitMessage = userAnswers
        .get(PartnerDetailsChangedPage)
        .contains(true)
    )
  }

  private def onPartnerDetailsRoute(
    partnerNumber: BusinessNumberOrIndex
  ): String =
    partnerNumber match {

      case _: Int =>
        routes.PartnerDetailsCheckYourAnswersController
          .onPageLoad(partnerNumber.toString)
          .url

      case _: String =>
        routes.PartnerDetailsCheckYourAnswersController
          .onPageLoad(partnerNumber.toString)
          .url
    }

  private def onRemoveRoute(
    partnerNumber: BusinessNumberOrIndex
  ): String =
    partnerNumber match {

      case _: Int =>
        routes.PartnerDetailsController
          .onRemove(
            partnerNumber.toString,
            NormalMode
          )
          .url

      case _: String =>
        routes.PartnerDetailsController
          .onRemove(
            partnerNumber.toString,
            CheckMode
          )
          .url
    }
}
