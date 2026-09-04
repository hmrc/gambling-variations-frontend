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

package viewmodels

import controllers.routes
import models.{BusinessType, UserAnswers}
import pages.*
import pages.businessaddress.BusinessAddressChangesPage
import pages.businessname.BusinessNameChangesPage
import pages.contactdetails.ContactDetailsChangesPage
import pages.correspondencedetails.CorrespondenceDetailsChangesPage
import pages.tradingdetails.TradingDetailsChangesPage
import play.api.i18n.Messages

final case class ChangeRegistrationDetailsViewModel(
  mgdRegNumber: String,
  managementHomeUrl: String,
  submitUrl: String,
  sections: Seq[RegistrationSectionRow]
) {

  def hasChanges: Boolean =
    sections.exists(_.status == ChangesReadyToSubmit)

  def showSubmit: Boolean = hasChanges

  def showNoChangesMessage: Boolean = !hasChanges
}

object ChangeRegistrationDetailsViewModel {

  def from(
    userAnswers: UserAnswers,
    mgdRegNumber: String,
    managementHomeUrl: String,
    isGroupMember: Boolean
  )(implicit messages: Messages): ChangeRegistrationDetailsViewModel = {

    val isPartnership =
      userAnswers.get(BusinessTypePage).contains(BusinessType.Partnership)

    def status(page: QuestionPage[Boolean]): SectionStatus =
      if (userAnswers.get(page).getOrElse(false)) ChangesReadyToSubmit else NoDetailsChanged

    val sections: Seq[RegistrationSectionRow] =
      Seq(
        optional(isGroupMember)(
          RegistrationSectionRow(
            messages("changeRegistrationDetails.controllingBodyDetails"),
            routes.PageNotFoundController.onPageLoad().url,
            NoDetailsChanged
          )
        ),
        optional(isGroupMember)(
          RegistrationSectionRow(
            messages("changeRegistrationDetails.groupMemberDetails"),
            routes.PageNotFoundController.onPageLoad().url,
            NoDetailsChanged
          )
        ),
        optional(!isGroupMember)(
          RegistrationSectionRow(
            messages("changeRegistrationDetails.businessName"),
            routes.CheckBusinessNameController.onPageLoad().url,
            status(BusinessNameChangesPage)
          )
        ),
        optional(!isGroupMember)(
          RegistrationSectionRow(
            messages("changeRegistrationDetails.businessAddress"),
            routes.CheckBusinessAddressController.onPageLoad().url,
            status(BusinessAddressChangesPage)
          )
        ),
        optional(!isGroupMember)(
          RegistrationSectionRow(
            messages("changeRegistrationDetails.businessContactDetails"),
            routes.CheckContactDetailsController.onPageLoad().url,
            status(ContactDetailsChangesPage)
          )
        ),
        Some(
          RegistrationSectionRow(
            messages("changeRegistrationDetails.correspondenceDetails"),
            routes.CheckCorrespondenceDetailsController.onPageLoad().url,
            status(CorrespondenceDetailsChangesPage)
          )
        ),
        optional(isPartnership)(
          RegistrationSectionRow(
            messages("changeRegistrationDetails.partnerDetails"),
            controllers.partner.routes.PartnerDetailsController.onPageLoad.url,
            NoDetailsChanged
          )
        ),
        Some(
          RegistrationSectionRow(
            messages("changeRegistrationDetails.tradingDetails"),
            routes.CheckTradingDetailsController.onPageLoad().url,
            status(TradingDetailsChangesPage)
          )
        ),
        optional(!isGroupMember)(
          RegistrationSectionRow(
            messages("changeRegistrationDetails.licencesAndPremises"),
            routes.PageNotFoundController.onPageLoad().url,
            NoDetailsChanged
          )
        ),
        Some(
          RegistrationSectionRow(
            messages("changeRegistrationDetails.returnPeriod"),
            routes.PageNotFoundController.onPageLoad().url,
            NoDetailsChanged
          )
        )
      ).flatten

    ChangeRegistrationDetailsViewModel(
      mgdRegNumber      = mgdRegNumber,
      managementHomeUrl = managementHomeUrl,
      submitUrl         = routes.DeclarationController.onPageLoad().url,
      sections          = sections
    )
  }

  private def optional(condition: Boolean)(row: => RegistrationSectionRow): Option[RegistrationSectionRow] =
    if (condition) Some(row) else None
}
