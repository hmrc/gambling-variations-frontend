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

import base.SpecBase
import controllers.licencespremises.routes
import models.UserAnswers
import models.licencespremises.LicencesAndPremisesRadioOptions.{ByPost, Online}
import models.licencespremises.OtherLicencesAndPermitsGB.{clubMachine, clubPremises, familyEntertainment, prizeGaming}
import models.licencespremises.OtherLicencesAndPermitsNI.{amusement, bingo, regCert, serveAlcohol}
import models.licencespremises.*
import org.jsoup.Jsoup
import pages.licencespremises.*
import play.api.i18n.Messages
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import scala.jdk.CollectionConverters.*

class CheckLicencesAndPremisesViewModelSpec extends SpecBase {

  private implicit lazy val msgs: Messages = messages(applicationBuilder().build())

  private val licenceNumber = "123-456789-A-123456-789"

  private def viewModel(
    licenceNumber: Option[String] = None,
    isPubTenant: Boolean = false,
    licencesAndPermitsGB: Seq[OtherLicencesAndPermitsGB] = Nil,
    licencesAndPermitsNI: Seq[OtherLicencesAndPermitsNI] = Nil,
    hasPremisesNotCovered: Boolean = false,
    provideAddressesAnswer: Option[LicencesAndPremisesRadioOptions] = None,
    premisesCount: Int = 0,
    isSubmitted: Boolean = false
  ): CheckLicencesAndPremisesViewModel =
    CheckLicencesAndPremisesViewModel(
      licenceNumber          = licenceNumber,
      isPubTenant            = isPubTenant,
      licencesAndPermitsGB   = licencesAndPermitsGB,
      licencesAndPermitsNI   = licencesAndPermitsNI,
      hasPremisesNotCovered  = hasPremisesNotCovered,
      provideAddressesAnswer = provideAddressesAnswer,
      premisesCount          = premisesCount,
      isSubmitted            = isSubmitted
    )

  private def keys(vm: CheckLicencesAndPremisesViewModel): Seq[String] =
    vm.summaryList.rows.map(_.key.content.asInstanceOf[Text].value)

  private def rowFor(vm: CheckLicencesAndPremisesViewModel, key: String): SummaryListRow =
    vm.summaryList.rows.find(_.key.content == Text(msgs(key))).value

  private def valueText(row: SummaryListRow): String =
    Jsoup.parse(row.value.content.asHtml.toString).text()

  private def listItems(row: SummaryListRow): Seq[String] =
    Jsoup.parse(row.value.content.asHtml.toString).select("li").asScala.map(_.text()).toSeq

  private def hrefs(row: SummaryListRow): Seq[String] =
    row.actions.value.items.map(_.href)

  "CheckLicencesAndPremisesViewModel" - {

    "when no licences or permits have been provided" - {

      "must not show the premises not covered row but show how addresses are provided" in {
        val vm = viewModel()

        keys(vm) mustEqual Seq(
          msgs("checkLicenceAndPremises.licenceNumber"),
          msgs("checkLicenceAndPremises.pubTenant"),
          msgs("checkLicenceAndPremises.licencesAndPermitsGB"),
          msgs("checkLicenceAndPremises.licencesAndPermitsNI"),
          msgs("checkLicenceAndPremises.provideAddresses")
        )
      }

      "must show default values" in {
        val vm = viewModel()

        valueText(rowFor(vm, "checkLicenceAndPremises.licenceNumber")) mustEqual msgs("site.notProvided")
        valueText(rowFor(vm, "checkLicenceAndPremises.pubTenant")) mustEqual msgs("site.no")
        valueText(rowFor(vm, "checkLicenceAndPremises.licencesAndPermitsGB")) mustEqual msgs("otherLicencesAndPermitsGB.option.none")
        valueText(rowFor(vm, "checkLicenceAndPremises.licencesAndPermitsNI")) mustEqual msgs("otherLicencesAndPermitsNI.option.none")
        valueText(rowFor(vm, "checkLicenceAndPremises.provideAddresses")) mustEqual msgs("site.notProvided")
      }

      "must show the no licences message and continue to LI-ME when the method is not provided" in {
        val vm = viewModel()

        vm.premisesDetailsRequiredMessage mustBe Some("checkLicenceAndPremises.noLicences.p1")
        vm.continueUrl mustEqual routes.LicencesPremisesController.onPageLoad().url
      }

      "must show the no licences message and continue to LI-ADLK-F when online is selected without any premises" in {
        val vm = viewModel(provideAddressesAnswer = Some(Online))

        vm.premisesDetailsRequiredMessage mustBe Some("checkLicenceAndPremises.noLicences.p1")
        vm.continueUrl mustEqual "#"
        valueText(rowFor(vm, "checkLicenceAndPremises.addressesOnline")) mustEqual "0 premises"
        hrefs(rowFor(vm, "checkLicenceAndPremises.addressesOnline")) mustEqual Seq("#")
      }

      "must show the premises count and continue to change registration details when premises have been provided" in {
        val vm = viewModel(premisesCount = 20)

        vm.premisesDetailsRequiredMessage mustBe None
        vm.showSendByPost mustBe false
        vm.continueUrl mustEqual controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
        valueText(rowFor(vm, "checkLicenceAndPremises.provideAddresses")) mustEqual msgs("checkLicenceAndPremises.provideAddresses.online")
        valueText(rowFor(vm, "checkLicenceAndPremises.addressesOnline")) mustEqual "20 premises"
      }

      "must show the post details and continue to change registration details when by post is selected" in {
        val vm = viewModel(provideAddressesAnswer = Some(ByPost))

        vm.premisesDetailsRequiredMessage mustBe None
        vm.showSendByPost mustBe true
        vm.continueUrl mustEqual controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
        valueText(rowFor(vm, "checkLicenceAndPremises.provideAddresses")) mustEqual msgs("checkLicenceAndPremises.provideAddresses.byPost")
        keys(vm) must not contain msgs("checkLicenceAndPremises.addressesOnline")
      }
    }

    "when licences or permits have been provided" - {

      "must treat any single licence or permit as provided" in {
        Seq(
          viewModel(licenceNumber        = Some(licenceNumber)),
          viewModel(isPubTenant          = true),
          viewModel(licencesAndPermitsGB = Seq(clubMachine)),
          viewModel(licencesAndPermitsNI = Seq(amusement))
        ).foreach { vm =>
          keys(vm) must contain(msgs("checkLicenceAndPremises.premisesNotCovered"))
        }
      }

      "must hide the premises rows and continue to change registration details when all premises are covered" in {
        val vm = viewModel(isPubTenant = true, provideAddressesAnswer = Some(ByPost), premisesCount = 3)

        keys(vm) mustEqual Seq(
          msgs("checkLicenceAndPremises.licenceNumber"),
          msgs("checkLicenceAndPremises.pubTenant"),
          msgs("checkLicenceAndPremises.licencesAndPermitsGB"),
          msgs("checkLicenceAndPremises.licencesAndPermitsNI"),
          msgs("checkLicenceAndPremises.premisesNotCovered")
        )
        valueText(rowFor(vm, "checkLicenceAndPremises.premisesNotCovered")) mustEqual msgs("site.no")
        vm.premisesDetailsRequiredMessage mustBe None
        vm.showSendByPost mustBe false
        vm.continueUrl mustEqual controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
      }

      "must show the not covered message and continue to LI-ME when premises are not covered and the method is not provided" in {
        val vm = viewModel(isPubTenant = true, hasPremisesNotCovered = true)

        keys(vm) must contain allOf (msgs("checkLicenceAndPremises.premisesNotCovered"), msgs("checkLicenceAndPremises.provideAddresses"))
        valueText(rowFor(vm, "checkLicenceAndPremises.premisesNotCovered")) mustEqual msgs("site.yes")
        vm.premisesDetailsRequiredMessage mustBe Some("checkLicenceAndPremises.premisesNotCovered.p1")
        vm.continueUrl mustEqual routes.LicencesPremisesController.onPageLoad().url
      }

      "must show the not covered message and continue to LI-ADLK-F when online is selected without any premises" in {
        val vm = viewModel(licencesAndPermitsNI = Seq(regCert), hasPremisesNotCovered = true, provideAddressesAnswer = Some(Online))

        vm.premisesDetailsRequiredMessage mustBe Some("checkLicenceAndPremises.premisesNotCovered.p1")
        vm.continueUrl mustEqual "#"
      }

      "must not show a message when premises are not covered and have been provided online" in {
        val vm = viewModel(licencesAndPermitsGB = Seq(prizeGaming), hasPremisesNotCovered = true, premisesCount = 10)

        vm.premisesDetailsRequiredMessage mustBe None
        vm.continueUrl mustEqual controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
        valueText(rowFor(vm, "checkLicenceAndPremises.addressesOnline")) mustEqual "10 premises"
      }

      "must prefer the answer given on LI-ME over the premises provided" in {
        val vm =
          viewModel(licenceNumber = Some(licenceNumber), hasPremisesNotCovered = true, provideAddressesAnswer = Some(ByPost), premisesCount = 10)

        vm.showSendByPost mustBe true
        keys(vm) must not contain msgs("checkLicenceAndPremises.addressesOnline")
      }
    }

    "must show the licence number with change and remove links" in {
      val row = rowFor(viewModel(licenceNumber = Some(licenceNumber)), "checkLicenceAndPremises.licenceNumber")

      valueText(row) mustEqual licenceNumber
      hrefs(row) mustEqual Seq(routes.LicenceNumberController.onPageLoad().url, routes.RemoveLicenceNumberController.onPageLoad().url)
      row.actions.value.items.map(_.visuallyHiddenText.value).distinct mustEqual Seq(msgs("checkLicenceAndPremises.licenceNumber.hidden"))
    }

    "must only show the change link for the licence number when it is not provided" in {
      val row = rowFor(viewModel(), "checkLicenceAndPremises.licenceNumber")

      hrefs(row) mustEqual Seq(routes.LicenceNumberController.onPageLoad().url)
    }

    "must list the licences and permits alphabetically" in {
      val vm = viewModel(
        licencesAndPermitsGB = Seq(prizeGaming, familyEntertainment, clubMachine),
        licencesAndPermitsNI = Seq(serveAlcohol, amusement)
      )

      listItems(rowFor(vm, "checkLicenceAndPremises.licencesAndPermitsGB")) mustEqual Seq(
        "Club machine permit",
        "Family entertainment centre gaming machine permit",
        "Prize gaming permit"
      )
      listItems(rowFor(vm, "checkLicenceAndPremises.licencesAndPermitsNI")) mustEqual Seq(
        "Amusement permit",
        "Licence allowing the serving of alcohol"
      )
    }

    "must link each change action to its question" in {
      val vm = viewModel(isPubTenant = true, hasPremisesNotCovered = true, premisesCount = 1)

      hrefs(rowFor(vm, "checkLicenceAndPremises.pubTenant")) mustEqual Seq(routes.LicenceDetailsLandlordLicenceYesNoController.onPageLoad().url)
      hrefs(rowFor(vm, "checkLicenceAndPremises.licencesAndPermitsGB")) mustEqual Seq(routes.OtherLicencesAndPermitsGBController.onPageLoad().url)
      hrefs(rowFor(vm, "checkLicenceAndPremises.licencesAndPermitsNI")) mustEqual Seq(routes.OtherLicencesAndPermitsNIController.onPageLoad().url)
      hrefs(rowFor(vm, "checkLicenceAndPremises.premisesNotCovered")) mustEqual Seq(routes.PremisesNotCoveredYesNoController.onPageLoad().url)
      hrefs(rowFor(vm, "checkLicenceAndPremises.provideAddresses")) mustEqual Seq(routes.LicencesPremisesController.onPageLoad().url)
      hrefs(rowFor(vm, "checkLicenceAndPremises.addressesOnline")) mustEqual Seq("#")
    }

    "must escape values" in {
      val row = rowFor(viewModel(licenceNumber = Some("<b>123</b>")), "checkLicenceAndPremises.licenceNumber")

      row.value.content.asHtml.toString must include("&lt;b&gt;123&lt;/b&gt;")
    }

    "from" - {

      "must use the backend values when the questions have not been answered in this session" in {
        val answers = UserAnswers(
          userAnswersId,
          Json.obj(
            "licencesPremisesSection" -> Json.obj(
              "mgdRegNum"             -> mgdRegNum,
              "haveGamblingLicenceNo" -> "1",
              "gamblingLicenceNo"     -> licenceNumber,
              "heldByLandlord"        -> "1",
              "clubLicence"           -> "1",
              "clubPremises"          -> "1",
              "prizeGaming"           -> "0",
              "bingo"                 -> "1",
              "premisesNotCovered"    -> "1",
              "premisesDetails"       -> Json.toJson(PremisesDetailsResponse(Some(12), Nil))
            )
          )
        )

        CheckLicencesAndPremisesViewModel.from(answers) mustEqual CheckLicencesAndPremisesViewModel(
          licenceNumber          = Some(licenceNumber),
          isPubTenant            = true,
          licencesAndPermitsGB   = Seq(clubMachine, clubPremises),
          licencesAndPermitsNI   = Seq(bingo),
          hasPremisesNotCovered  = true,
          provideAddressesAnswer = None,
          premisesCount          = 12,
          isSubmitted            = false
        )
      }

      "must use the answers given in this session over the backend values" in {
        val answers = emptyUserAnswers
          .set(LicenceHeldByLandlordPage, "1")
          .success
          .value
          .set(LicenceDetailsLandlordLicenceYesNoPage, false)
          .success
          .value
          .set(LicencePremisesNotCoveredPage, "0")
          .success
          .value
          .set(PremisesNotCoveredYesNoPage, true)
          .success
          .value
          .set(LicencesPremisesPage, ByPost)
          .success
          .value
          .set(LicencesPremisesDetailsSubmittedPage, true)
          .success
          .value

        val vm = CheckLicencesAndPremisesViewModel.from(answers)

        vm.isPubTenant mustBe false
        vm.hasPremisesNotCovered mustBe true
        vm.provideAddressesAnswer mustBe Some(ByPost)
        vm.isSubmitted mustBe true
      }

      "must count the premises when the total is missing and treat a blank licence number as not provided" in {
        val premises = PremisesDetails(mgdRegNum, Some("1 Street"), None, None, None, Some("AA1 1AA"), None)
        val answers = emptyUserAnswers
          .set(PremisesDetailsPage, PremisesDetailsResponse(None, Seq(premises, premises)))
          .success
          .value
          .set(LicenceNumberPage, " ")
          .success
          .value

        val vm = CheckLicencesAndPremisesViewModel.from(answers)

        vm.premisesCount mustBe 2
        vm.licenceNumber mustBe None
      }

      "must convert the backend flags when these are 0" in {
        val answers = UserAnswers(
          userAnswersId,
          Json.obj(
            "licencesPremisesSection" -> Json.obj(
              "heldByLandlord"     -> "0",
              "clubLicence"        -> "0",
              "bingo"              -> "0",
              "premisesNotCovered" -> "0"
            )
          )
        )

        CheckLicencesAndPremisesViewModel.from(answers) mustEqual viewModel()
      }

      "must throw an exception when a backend flag is neither 1 nor 0" in {
        Seq(
          LicenceHeldByLandlordPage,
          LicencePremisesNotCoveredPage,
          OtherLicencesAndPermitsGB.mappedValuesWithPages(clubMachine),
          OtherLicencesAndPermitsNI.mappedValuesWithPages(bingo)
        ).foreach { page =>
          val answers = emptyUserAnswers.set(page, "Yes").success.value

          an[IllegalArgumentException] mustBe thrownBy {
            CheckLicencesAndPremisesViewModel.from(answers)
          }
        }
      }

      "must default to no answers when the section is empty" in {
        CheckLicencesAndPremisesViewModel.from(emptyUserAnswers) mustEqual viewModel()
      }
    }
  }
}
