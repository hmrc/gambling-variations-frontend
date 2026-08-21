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

import base.SpecBase
import controllers.routes
import models.{BusinessType, UserAnswers}
import pages.*
import pages.businessaddress.*
import pages.businessname.{BusinessNameChangesPage, BusinessNameSubmittedPage}
import pages.contactdetails.{BusinessContactDetailsSubmittedPage, ContactDetailsChangesPage}
import pages.correspondencedetails.{CorrespondenceDetailsChangesPage, CorrespondenceDetailsSubmittedPage}
import pages.tradingdetails.{TradingDetailsChangesPage, TradingDetailsSubmittedPage}
import play.api.Application
import play.api.i18n.Messages

class ChangeRegistrationDetailsViewModelSpec extends SpecBase {

  private val app: Application = applicationBuilder().build()

  private implicit val msgs: Messages = messages(app)

  private val managementHomeUrl = "http://foo.com/home"

  private def viewModel(userAnswers: UserAnswers, isGroupMember: Boolean): ChangeRegistrationDetailsViewModel =
    ChangeRegistrationDetailsViewModel.from(
      userAnswers       = userAnswers,
      mgdRegNumber      = mgdRegNum,
      managementHomeUrl = managementHomeUrl,
      isGroupMember     = isGroupMember
    )

  private def answersFor(businessType: BusinessType): UserAnswers =
    emptyUserAnswers.set(BusinessTypePage, businessType).success.value

  private val nonPartnershipAnswers = answersFor(BusinessType.Corporatebody)
  private val partnershipAnswers = answersFor(BusinessType.Partnership)

  "ChangeRegistrationDetailsViewModel.from" - {

    "must build the sections for a non group member who is not a partnership" in {

      val vm = viewModel(nonPartnershipAnswers, isGroupMember = false)

      vm.sections.map(_.name) mustEqual Seq(
        "Business name",
        "Business address",
        "Business contact details",
        "Correspondence details",
        "Trading details",
        "Licences and premises",
        "Return periods"
      )
    }

    "must build the sections for a non group member who is a partnership" in {

      val vm = viewModel(partnershipAnswers, isGroupMember = false)

      vm.sections.map(_.name) mustEqual Seq(
        "Business name",
        "Business address",
        "Business contact details",
        "Correspondence details",
        "Partner details",
        "Trading details",
        "Licences and premises",
        "Return periods"
      )
    }

    "must build the sections for a group member who is not a partnership" in {

      val vm = viewModel(nonPartnershipAnswers, isGroupMember = true)

      vm.sections.map(_.name) mustEqual Seq(
        "Controlling body details",
        "Group member details",
        "Correspondence details",
        "Trading details",
        "Return periods"
      )
    }

    "must show partner details for a group member who is a partnership" in {

      val vm = viewModel(partnershipAnswers, isGroupMember = true)

      vm.sections.map(_.name) mustEqual Seq(
        "Controlling body details",
        "Group member details",
        "Correspondence details",
        "Partner details",
        "Trading details",
        "Return periods"
      )
    }

    "must omit partner details when the business type is missing" in {

      val vm = viewModel(emptyUserAnswers, isGroupMember = false)

      vm.sections.map(_.name) must not contain "Partner details"
    }

    "must link the sections that are already built" in {

      val vm = viewModel(nonPartnershipAnswers, isGroupMember = false)

      def urlOf(name: String): String = vm.sections.find(_.name == name).value.url

      urlOf("Business name") mustEqual routes.CheckBusinessNameController.onPageLoad().url
      urlOf("Business address") mustEqual routes.BusinessUKAddressController.onPageLoad().url
      urlOf("Business contact details") mustEqual routes.CheckContactDetailsController.onPageLoad().url
      urlOf("Correspondence details") mustEqual routes.CheckCorrespondenceDetailsController.onPageLoad().url
      urlOf("Trading details") mustEqual routes.CheckTradingDetailsController.onPageLoad().url
    }

    "must send the sections that are not built yet to the page not found screen" in {

      // These rows are shown so the user sees the full picture, but the journeys behind them do
      // not exist. Remove a name from this list as each journey is built.
      val notBuiltYet = Set(
        "Controlling body details",
        "Group member details",
        "Partner details",
        "Licences and premises",
        "Return periods"
      )

      val allSections =
        viewModel(partnershipAnswers, isGroupMember = false).sections ++
          viewModel(partnershipAnswers, isGroupMember = true).sections

      val placeholderUrls =
        allSections.filter(section => notBuiltYet.contains(section.name)).map(_.url).distinct

      placeholderUrls mustEqual Seq(routes.PageNotFoundController.onPageLoad().url)
    }

    "must point the submit link at the declaration page" in {

      val vm = viewModel(nonPartnershipAnswers, isGroupMember = false)

      vm.submitUrl mustEqual routes.DeclarationController.onPageLoad().url
    }

    "must carry the registration number and the management home url" in {

      val vm = viewModel(nonPartnershipAnswers, isGroupMember = false)

      vm.mgdRegNumber mustEqual mgdRegNum
      vm.managementHomeUrl mustEqual managementHomeUrl
    }

    "must default every section to no details changed" in {

      val vm = viewModel(partnershipAnswers, isGroupMember = false)

      vm.sections.map(_.status).distinct mustEqual Seq(NoDetailsChanged)
    }

    val changeFlags = Seq(
      (BusinessNameChangesPage, "Business name"),
      (BusinessAddressChangesPage, "Business address"),
      (ContactDetailsChangesPage, "Business contact details"),
      (CorrespondenceDetailsChangesPage, "Correspondence details"),
      (TradingDetailsChangesPage, "Trading details")
    )

    changeFlags.foreach { case (page, sectionName) =>
      s"must mark only $sectionName as ready to submit when its change flag is set" in {

        val userAnswers = nonPartnershipAnswers.set(page, true).success.value

        val vm = viewModel(userAnswers, isGroupMember = false)

        vm.sections.filter(_.status == ChangesReadyToSubmit).map(_.name) mustEqual Seq(sectionName)
      }
    }

    "must ignore the submitted flags when working out the status" in {

      // The submitted flags only record that the user has walked through a section. On their own
      // they are not a change, so the section must still read as no details changed.
      val userAnswers =
        nonPartnershipAnswers
          .set(BusinessNameSubmittedPage, true)
          .success
          .value
          .set(BusinessContactDetailsSubmittedPage, true)
          .success
          .value
          .set(CorrespondenceDetailsSubmittedPage, true)
          .success
          .value
          .set(TradingDetailsSubmittedPage, true)
          .success
          .value

      val vm = viewModel(userAnswers, isGroupMember = false)

      vm.sections.map(_.status).distinct mustEqual Seq(NoDetailsChanged)
      vm.hasChanges mustEqual false
    }
  }

  "ChangeRegistrationDetailsViewModel" - {

    "must not offer the submit button when nothing has changed" in {

      val vm = viewModel(nonPartnershipAnswers, isGroupMember = false)

      vm.hasChanges mustEqual false
      vm.showSubmit mustEqual false
      vm.showNoChangesMessage mustEqual true
    }

    "must offer the submit button when at least one section has changes" in {

      val userAnswers = nonPartnershipAnswers.set(TradingDetailsChangesPage, true).success.value

      val vm = viewModel(userAnswers, isGroupMember = false)

      vm.hasChanges mustEqual true
      vm.showSubmit mustEqual true
      vm.showNoChangesMessage mustEqual false
    }

    "must not offer the submit button when the only outstanding section needs completing" in {

      val vm =
        ChangeRegistrationDetailsViewModel(
          mgdRegNumber      = mgdRegNum,
          managementHomeUrl = managementHomeUrl,
          submitUrl         = routes.DeclarationController.onPageLoad().url,
          sections = Seq(
            RegistrationSectionRow("Controlling body details", "/controlling-body", NeedsCompleting),
            RegistrationSectionRow("Trading details", "/trading-details", NoDetailsChanged)
          )
        )

      vm.hasChanges mustEqual false
      vm.showSubmit mustEqual false
    }
  }
}
