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

package viewmodels.checkAnswers.licencespremises

import controllers.licencespremises.routes
import models.UserAnswers
import models.licencespremises.LicencesAndPremisesRadioOptions.{ByPost, Online}
import models.licencespremises.{LicencesAndPremisesRadioOptions, OtherLicencesAndPermitsGB, OtherLicencesAndPermitsNI}
import pages.QuestionPage
import pages.licencespremises.*
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import utils.FlagsUtil.checkFlag
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

case class CheckLicencesAndPremisesViewModel(
  licenceNumber: Option[String],
  isPubTenant: Boolean,
  licencesAndPermitsGB: Seq[OtherLicencesAndPermitsGB],
  licencesAndPermitsNI: Seq[OtherLicencesAndPermitsNI],
  hasPremisesNotCovered: Boolean,
  provideAddressesAnswer: Option[LicencesAndPremisesRadioOptions],
  premisesCount: Int,
  isSubmitted: Boolean
) {

  // The LI-AD and LI-ADLK-F screens are not built yet
  private val premisesAddressesUrl = "#"
  private val findPremisesAddressUrl = "#"

  // LI-NT is only asked when the user has provided some kind of licence or permit
  private val hasLicencesOrPermits: Boolean =
    licenceNumber.isDefined || isPubTenant || licencesAndPermitsGB.nonEmpty || licencesAndPermitsNI.nonEmpty

  // Premises must be provided if there are no licences or permits, or if these do not cover all premises
  private val isPremisesDetailsRequired: Boolean =
    !hasLicencesOrPermits || hasPremisesNotCovered

  // There is no stored value for the method, so it is derived from the premises when LI-ME has not been answered
  private val provideAddresses: Option[LicencesAndPremisesRadioOptions] =
    provideAddressesAnswer orElse Option.when(premisesCount > 0)(Online)

  private val isMissingProvideAddresses: Boolean =
    isPremisesDetailsRequired && provideAddresses.isEmpty

  private val isMissingOnlinePremises: Boolean =
    isPremisesDetailsRequired && provideAddresses.contains(Online) && premisesCount == 0

  val premisesDetailsRequiredMessage: Option[String] =
    Option.when(isMissingProvideAddresses || isMissingOnlinePremises) {
      if (hasLicencesOrPermits) "checkLicenceAndPremises.premisesNotCovered.p1" else "checkLicenceAndPremises.noLicences.p1"
    }

  val showSendByPost: Boolean =
    isPremisesDetailsRequired && provideAddresses.contains(ByPost)

  val continueUrl: String =
    if (isMissingProvideAddresses) {
      routes.LicencesPremisesController.onPageLoad().url
    } else if (isMissingOnlinePremises) {
      findPremisesAddressUrl
    } else {
      controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
    }

  def summaryList(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        Some(licenceNumberRow),
        Some(pubTenantRow),
        Some(licencesAndPermitsGBRow),
        Some(licencesAndPermitsNIRow),
        Option.when(hasLicencesOrPermits)(premisesNotCoveredRow),
        Option.when(isPremisesDetailsRequired)(provideAddressesRow),
        Option.when(isPremisesDetailsRequired && provideAddresses.contains(Online))(addressesOnlineRow)
      ).flatten
    ).withCssClass("check-licences-premises-list")

  private def licenceNumberRow(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key   = "checkLicenceAndPremises.licenceNumber",
      value = ValueViewModel(Text(licenceNumber.getOrElse(messages("site.notProvided")))),
      actions = Seq(
        Some(changeAction(routes.LicenceNumberController.onPageLoad().url, "checkLicenceAndPremises.licenceNumber.hidden")),
        licenceNumber.map(_ => removeAction(routes.RemoveLicenceNumberController.onPageLoad().url, "checkLicenceAndPremises.licenceNumber.hidden"))
      ).flatten
    )

  private def pubTenantRow(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key     = "checkLicenceAndPremises.pubTenant",
      value   = ValueViewModel(yesNo(isPubTenant)),
      actions = Seq(changeAction(routes.LicenceDetailsLandlordLicenceYesNoController.onPageLoad().url, "checkLicenceAndPremises.pubTenant.hidden"))
    )

  private def licencesAndPermitsGBRow(implicit messages: Messages): SummaryListRow =
    licencesAndPermitsRow(
      key             = "checkLicenceAndPremises.licencesAndPermitsGB",
      selectedOptions = licencesAndPermitsGB.map(value => messages(s"otherLicencesAndPermitsGB.option.$value")),
      noneSelected    = messages("otherLicencesAndPermitsGB.option.none"),
      changeUrl       = routes.OtherLicencesAndPermitsGBController.onPageLoad().url
    )

  private def licencesAndPermitsNIRow(implicit messages: Messages): SummaryListRow =
    licencesAndPermitsRow(
      key             = "checkLicenceAndPremises.licencesAndPermitsNI",
      selectedOptions = licencesAndPermitsNI.map(value => messages(s"otherLicencesAndPermitsNI.option.$value")),
      noneSelected    = messages("otherLicencesAndPermitsNI.option.none"),
      changeUrl       = routes.OtherLicencesAndPermitsNIController.onPageLoad().url
    )

  private def licencesAndPermitsRow(key: String, selectedOptions: Seq[String], noneSelected: String, changeUrl: String)(implicit
    messages: Messages
  ): SummaryListRow = {
    val value =
      if (selectedOptions.isEmpty) {
        ValueViewModel(Text(noneSelected))
      } else {
        val items = selectedOptions.sorted.map(option => s"<li>${HtmlFormat.escape(option)}</li>").mkString
        ValueViewModel(HtmlContent(s"""<ul class="govuk-list govuk-list--bullet">$items</ul>"""))
      }

    SummaryListRowViewModel(
      key     = key,
      value   = value,
      actions = Seq(changeAction(changeUrl, s"$key.hidden"))
    )
  }

  private def premisesNotCoveredRow(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key     = "checkLicenceAndPremises.premisesNotCovered",
      value   = ValueViewModel(yesNo(hasPremisesNotCovered)),
      actions = Seq(changeAction(routes.PremisesNotCoveredYesNoController.onPageLoad().url, "checkLicenceAndPremises.premisesNotCovered.hidden"))
    )

  private def provideAddressesRow(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key = "checkLicenceAndPremises.provideAddresses",
      value = ValueViewModel(
        Text(provideAddresses.map(answer => messages(s"checkLicenceAndPremises.provideAddresses.$answer")).getOrElse(messages("site.notProvided")))
      ),
      actions = Seq(changeAction(routes.LicencesPremisesController.onPageLoad().url, "checkLicenceAndPremises.provideAddresses.hidden"))
    )

  private def addressesOnlineRow(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key   = "checkLicenceAndPremises.addressesOnline",
      value = ValueViewModel(Text(messages("checkLicenceAndPremises.addressesOnline.value", premisesCount))),
      actions = Seq(
        changeAction(
          if (premisesCount > 0) premisesAddressesUrl else findPremisesAddressUrl,
          "checkLicenceAndPremises.addressesOnline.hidden"
        )
      )
    )

  private def changeAction(href: String, hiddenText: String)(implicit messages: Messages): ActionItem =
    ActionItemViewModel("site.change", href).withVisuallyHiddenText(messages(hiddenText))

  private def removeAction(href: String, hiddenText: String)(implicit messages: Messages): ActionItem =
    ActionItemViewModel("site.remove", href).withVisuallyHiddenText(messages(hiddenText))

  private def yesNo(answer: Boolean)(implicit messages: Messages): String =
    if (answer) messages("site.yes") else messages("site.no")
}

object CheckLicencesAndPremisesViewModel {

  def from(answers: UserAnswers): CheckLicencesAndPremisesViewModel =
    CheckLicencesAndPremisesViewModel(
      licenceNumber = answers.get(LicenceNumberPage).map(_.trim).filter(_.nonEmpty),
      // The yes/no pages hold the answers given in this session, otherwise the flags from the backend apply
      isPubTenant = answers.get(LicenceDetailsLandlordLicenceYesNoPage).getOrElse(flag(answers, LicenceHeldByLandlordPage)),
      licencesAndPermitsGB =
        OtherLicencesAndPermitsGB.positiveValues.filter(value => flag(answers, OtherLicencesAndPermitsGB.mappedValuesWithPages(value))),
      licencesAndPermitsNI =
        OtherLicencesAndPermitsNI.positiveValues.filter(value => flag(answers, OtherLicencesAndPermitsNI.mappedValuesWithPages(value))),
      hasPremisesNotCovered  = answers.get(PremisesNotCoveredYesNoPage).getOrElse(flag(answers, LicencePremisesNotCoveredPage)),
      provideAddressesAnswer = answers.get(LicencesPremisesPage),
      premisesCount          = answers.get(PremisesDetailsPage).map(details => details.totalRows.getOrElse(details.premises.size)).getOrElse(0),
      isSubmitted            = checkFlag(answers, LicencesPremisesDetailsChangesPage, LicencesPremisesDetailsSubmittedPage)
    )

  // The backend holds booleans as "1" or "0", an oracle implementation detail that has been propagated through 3 layers of microservices.
  // A missing flag is treated as indicating false.
  private def flag(answers: UserAnswers, page: QuestionPage[String]): Boolean =
    answers.get(page).fold(false) {
      case "1"   => true
      case "0"   => false
      case value => throw new IllegalArgumentException(s"Unexpected value '$value' for $page, expected 1 or 0")
    }
}
