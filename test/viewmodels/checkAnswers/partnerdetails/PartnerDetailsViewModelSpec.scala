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

import base.SpecBase
import config.FrontendAppConfig
import controllers.partnerdetails.PartnerDetailsHelper
import models.{BusinessType, UserAnswers}
import pages.BusinessNumberOrIndex
import pages.partnerdetails.*
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.collection.Seq

class PartnerDetailsViewModelSpec extends SpecBase with PartnerDetailsHelper {

  private val paginationHelper1 = PaginationHelper(1)
  private val paginationHelper2 = PaginationHelper(2)
  private val paginationHelper3 = PaginationHelper(3)
  private val paginationHelper10 = PaginationHelper(10)

  private val businessName1 =
    "business1"

  private val application =
    applicationBuilder()
      .configure(
        "partner-details.max-partners" -> 10
      )
      .build()

  private implicit val messages: Messages =
    application.injector
      .instanceOf[MessagesApi]
      .preferred(FakeRequest())

  private val frontendAppConfig =
    new FrontendAppConfig(application.configuration)

  private val dateFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy")

  /*
   * Most unit tests are not concerned with pagination.
   * For those tests, the same partner list represents both:
   *
   *  - partners displayed on the page
   *  - all partners used for business-rule counts
   */
  private def buildViewModel(
    partnerNumbers: Seq[BusinessNumberOrIndex],
    userAnswers: UserAnswers
  ): PartnerDetailsViewModel =
    PartnerDetailsViewModel.from(
      paginatedPartnerNumbers = partnerNumbers,
      allPartnerNumbers       = partnerNumbers,
      todayDate               = todayDate,
      userAnswers             = userAnswers,
      frontendAppConfig       = frontendAppConfig
    )

  /*
   * Use this overload when the partners displayed on the page
   * are different from the complete partner collection.
   */
  private def buildViewModel(
    paginatedPartnerNumbers: Seq[BusinessNumberOrIndex],
    allPartnerNumbers: Seq[BusinessNumberOrIndex],
    userAnswers: UserAnswers
  ): PartnerDetailsViewModel =
    PartnerDetailsViewModel.from(
      paginatedPartnerNumbers = paginatedPartnerNumbers,
      allPartnerNumbers       = allPartnerNumbers,
      todayDate               = todayDate,
      userAnswers             = userAnswers,
      frontendAppConfig       = frontendAppConfig
    )

  private def answersForPartners(
    partnerNumbers: Seq[BusinessNumberOrIndex]
  ): UserAnswers =
    partnerNumbers.foldLeft(emptyUserAnswers) { case (userAnswers, partnerNumber) =>
      userAnswers
        .set(
          PartnerDetailsMgdRegNumberPage(partnerNumber),
          s"XWM-$partnerNumber"
        )
        .success
        .value
        .set(
          PartnerDetailsBusinessTypePage(partnerNumber),
          BusinessType.LimitedLiabilityPartnership
        )
        .success
        .value
        .set(
          PartnerDetailsBusinessNamePage(partnerNumber),
          s"Business $partnerNumber"
        )
        .success
        .value
    }

  "PartnerDetailsViewModel" - {

    "show no partners message when no partners exist" in {

      val viewModel =
        PartnerDetailsViewModel.from(
          paginatedPartnerNumbers = Seq.empty,
          allPartnerNumbers       = Seq.empty,
          todayDate               = todayDate,
          userAnswers             = emptyUserAnswers,
          frontendAppConfig       = frontendAppConfig
        )

      viewModel.partners mustBe empty
      viewModel.showNoPartnersMessage mustBe true
      viewModel.showSubmitMessage mustBe false
      viewModel.addAnotherPartner mustBe true
      viewModel.showMinimumPartnersMessage mustBe false
      viewModel.showMaximumPartnersMessage mustBe false
    }

    "use business name when partner name details are present" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            businessName1
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.Partnership
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      viewModel.partners must have size 1
      viewModel.partners.head.name mustBe businessName1
    }

    "use trading and business-name partner details correctly" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsTradingNamePage(businessNumber1),
            "XYZ Trading"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.Partnership
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            businessName1
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      viewModel.partners.head.name mustBe businessName1
    }

    "use business name when trading name is absent" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            "XYZ Business"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.Partnership
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      viewModel.partners.head.name mustBe "XYZ Business"
    }

    "show active status when no future join or leave dates exist" in {

      val pastDate =
        LocalDate.of(2025, 1, 1)

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfJoiningPage(businessNumber1),
            pastDate
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.LimitedLiabilityPartnership
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            businessName1
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      val row =
        viewModel.partners.head

      row.status mustBe messages(
        "partnerDetails.status.active",
        pastDate.format(dateFormatter)
      )

      row.statusDetails mustBe None
    }

    "show due to join status for a new partner when joining date is in the past" in {

      val newPartnerIndex = 0
      val pastJoiningDate =
        todayDate.minusDays(10)

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsAddPartnerCompletedPage(newPartnerIndex),
            true
          )
          .success
          .value
          .set(
            PartnerDetailsMgdRegNumberPage(newPartnerIndex),
            emptyUserAnswers.id
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfJoiningPage(newPartnerIndex),
            pastJoiningDate
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(newPartnerIndex),
            BusinessType.LimitedLiabilityPartnership
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(newPartnerIndex),
            "New Partner"
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = Seq(newPartnerIndex),
          userAnswers    = answers
        )

      val row =
        viewModel.partners.head

      row.status mustBe messages(
        "partnerDetails.status.dueToJoin"
      )

      row.statusDetails mustBe Some(
        pastJoiningDate.format(dateFormatter)
      )
    }

    "show due to join status when joining date is in the future" in {

      val joinDate =
        todayDate.plusDays(10)

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfJoiningPage(businessNumber1),
            joinDate
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.LimitedLiabilityPartnership
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            businessName1
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      val row =
        viewModel.partners.head

      row.status mustBe messages(
        "partnerDetails.status.dueToJoin"
      )

      row.statusDetails mustBe Some(
        joinDate.format(dateFormatter)
      )
    }

    "show due to leave status when leaving date is in the future" in {

      val leaveDate =
        todayDate.plusDays(10)

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(businessNumber1),
            leaveDate
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.LimitedLiabilityPartnership
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            businessName1
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      val row =
        viewModel.partners.head

      row.status mustBe messages(
        "partnerDetails.status.dueToLeave"
      )

      row.statusDetails mustBe Some(
        leaveDate.format(dateFormatter)
      )
    }

    "prioritise due to leave over due to join" in {

      val joiningDate =
        todayDate.plusDays(20)

      val leavingDate =
        todayDate.plusDays(10)

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfJoiningPage(businessNumber1),
            joiningDate
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(businessNumber1),
            leavingDate
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.LimitedLiabilityPartnership
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            businessName1
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      viewModel.partners.head.status mustBe
        messages("partnerDetails.status.dueToLeave")

      viewModel.partners.head.statusDetails mustBe
        Some(leavingDate.format(dateFormatter))
    }

    "allow partner removal when leaving date is absent and more than two active partners exist" in {

      val partnerNumbers =
        paginationHelper3.partnerDetailsBusinessNumberList

      val answers =
        answersForPartners(partnerNumbers)

      val viewModel =
        buildViewModel(
          partnerNumbers = partnerNumbers,
          userAnswers    = answers
        )

      viewModel.partners.foreach { row =>
        row.canRemove mustBe true
        row.removeUrl mustBe defined
      }
    }

    "not allow partner removal when leaving date is present" in {

      val leavingDate =
        todayDate.plusDays(10)

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(businessNumber1),
            leavingDate
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.LimitedLiabilityPartnership
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            businessName1
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      val row =
        viewModel.partners.head

      row.canRemove mustBe false
      row.removeUrl mustBe None
    }

    "sort partners alphabetically by name" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "2"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            "Zulu"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.Partnership
          )
          .success
          .value
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber2),
            "1"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber2),
            "Alpha"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber2),
            BusinessType.Partnership
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper2.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      viewModel.partners.map(_.name) mustBe Seq(
        "Alpha",
        "Zulu"
      )
    }

    "sort partners case insensitively" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "1"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            "zulu"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.Partnership
          )
          .success
          .value
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber2),
            "2"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber2),
            "Alpha"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber2),
            BusinessType.Partnership
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper2.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      viewModel.partners.map(_.name) mustBe Seq(
        "Alpha",
        "zulu"
      )
    }

    "show due to join status when joining date is today" in {

      val joiningDate =
        todayDate

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfJoiningPage(businessNumber1),
            joiningDate
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.LimitedLiabilityPartnership
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            businessName1
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      val row =
        viewModel.partners.head

      row.status mustBe messages(
        "partnerDetails.status.dueToJoin"
      )

      row.statusDetails mustBe Some(
        joiningDate.format(dateFormatter)
      )
    }

    "show due to leave status and date when leaving date is today" in {

      val leavingDate =
        todayDate

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(businessNumber1),
            leavingDate
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.LimitedLiabilityPartnership
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            businessName1
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      val row =
        viewModel.partners.head

      row.status mustBe messages(
        "partnerDetails.status.dueToLeave"
      )

      row.statusDetails mustBe Some(
        leavingDate.format(dateFormatter)
      )
    }

    "show minimum partners message when fewer than three active partners exist" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsChangedPage,
            true
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(businessNumber1),
            BusinessType.LimitedLiabilityPartnership
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(businessNumber1),
            businessName1
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      viewModel.partners must have size 1
      viewModel.showMinimumPartnersMessage mustBe true
      viewModel.showSubmitMessage mustBe true
    }

    "not show minimum partners message when more than three active partners exist" in {

      val allPartnerNumbers: Seq[BusinessNumberOrIndex] =
        (0 until 4).map(_.toString)

      val answers =
        answersForPartners(allPartnerNumbers)
          .set(
            PartnerDetailsChangedPage,
            true
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          paginatedPartnerNumbers = allPartnerNumbers.take(1),
          allPartnerNumbers       = allPartnerNumbers,
          userAnswers             = answers
        )

      viewModel.partners must have size 1
      viewModel.showMinimumPartnersMessage mustBe false
      viewModel.showSubmitMessage mustBe true
    }

    "use all partners for minimum-partner rules rather than only the current page" in {

      val allPartnerNumbers =
        paginationHelper3.partnerDetailsBusinessNumberList

      val answers =
        answersForPartners(allPartnerNumbers)

      val viewModel =
        buildViewModel(
          paginatedPartnerNumbers = allPartnerNumbers.take(1),
          allPartnerNumbers       = allPartnerNumbers,
          userAnswers             = answers
        )

      viewModel.partners must have size 1
      viewModel.showMinimumPartnersMessage mustBe false
      viewModel.partners.head.canRemove mustBe true
    }

    "show maximum partners message when maximum number of partners without a leaving date is reached" in {

      val allPartnerNumbers =
        paginationHelper10.partnerDetailsBusinessNumberList

      val answers =
        answersForPartners(allPartnerNumbers)
          .set(
            PartnerDetailsChangedPage,
            true
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          paginatedPartnerNumbers = allPartnerNumbers,
          allPartnerNumbers       = allPartnerNumbers,
          userAnswers             = answers
        )

      viewModel.partners must have size 10
      viewModel.showMaximumPartnersMessage mustBe true
      viewModel.addAnotherPartner mustBe false
      viewModel.showSubmitMessage mustBe true
    }

    "use all partners for maximum limit when only one page is displayed" in {

      val allPartnerNumbers =
        paginationHelper10.partnerDetailsBusinessNumberList

      val answers =
        answersForPartners(allPartnerNumbers)

      val viewModel =
        buildViewModel(
          paginatedPartnerNumbers = allPartnerNumbers.take(2),
          allPartnerNumbers       = allPartnerNumbers,
          userAnswers             = answers
        )

      viewModel.partners must have size 2
      viewModel.showMaximumPartnersMessage mustBe true
      viewModel.addAnotherPartner mustBe false
    }

    "allow another partner when below maximum" in {

      val partnerNumbers =
        paginationHelper1.partnerDetailsBusinessNumberList

      val answers =
        answersForPartners(partnerNumbers)

      val viewModel =
        buildViewModel(
          partnerNumbers = partnerNumbers,
          userAnswers    = answers
        )

      viewModel.addAnotherPartner mustBe true
      viewModel.showMaximumPartnersMessage mustBe false
    }

    "exclude a partner with a future leaving date from the maximum count" in {

      val allPartnerNumbers =
        paginationHelper10.partnerDetailsBusinessNumberList

      val partnerWithLeavingDate =
        allPartnerNumbers.head

      val answers =
        answersForPartners(allPartnerNumbers)
          .set(
            PartnerDetailsDateOfLeavingPage(
              partnerWithLeavingDate
            ),
            todayDate.plusDays(10)
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          paginatedPartnerNumbers = allPartnerNumbers,
          allPartnerNumbers       = allPartnerNumbers,
          userAnswers             = answers
        )

      viewModel.addAnotherPartner mustBe true
      viewModel.showMaximumPartnersMessage mustBe false
    }

    "exclude a partner with a past leaving date from the maximum count" in {

      val allPartnerNumbers =
        paginationHelper10.partnerDetailsBusinessNumberList

      val partnerWithLeavingDate =
        allPartnerNumbers.head

      val answers =
        answersForPartners(allPartnerNumbers)
          .set(
            PartnerDetailsDateOfLeavingPage(
              partnerWithLeavingDate
            ),
            todayDate.minusDays(10)
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          paginatedPartnerNumbers = allPartnerNumbers,
          allPartnerNumbers       = allPartnerNumbers,
          userAnswers             = answers
        )

      viewModel.addAnotherPartner mustBe true
      viewModel.showMaximumPartnersMessage mustBe false
    }

    "exclude every partner having a leaving date from the maximum count" in {

      val allPartnerNumbers =
        paginationHelper10.partnerDetailsBusinessNumberList

      val firstLeavingPartner =
        allPartnerNumbers.head

      val secondLeavingPartner =
        allPartnerNumbers(1)

      val answers =
        answersForPartners(allPartnerNumbers)
          .set(
            PartnerDetailsDateOfLeavingPage(
              firstLeavingPartner
            ),
            todayDate.minusDays(10)
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(
              secondLeavingPartner
            ),
            todayDate.plusDays(10)
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          paginatedPartnerNumbers = allPartnerNumbers,
          allPartnerNumbers       = allPartnerNumbers,
          userAnswers             = answers
        )

      viewModel.addAnotherPartner mustBe true
      viewModel.showMaximumPartnersMessage mustBe false
    }

    "include a future-joining partner in the maximum count" in {

      val allPartnerNumbers =
        paginationHelper10.partnerDetailsBusinessNumberList

      val futureJoiningPartner =
        allPartnerNumbers.head

      val answers =
        answersForPartners(allPartnerNumbers)
          .set(
            PartnerDetailsDateOfJoiningPage(
              futureJoiningPartner
            ),
            todayDate.plusDays(10)
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          paginatedPartnerNumbers = allPartnerNumbers,
          allPartnerNumbers       = allPartnerNumbers,
          userAnswers             = answers
        )

      viewModel.addAnotherPartner mustBe false
      viewModel.showMaximumPartnersMessage mustBe true
    }

    "show submit message when partner details have been changed" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsChangedPage,
            true
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      viewModel.showSubmitMessage mustBe true
    }

    "not show submit message when partner details have not been changed" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(businessNumber1),
            "XWM00000001762"
          )
          .success
          .value

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = answers
        )

      viewModel.showSubmitMessage mustBe false
    }

    "not show submit message when there are no partners" in {

      val viewModel =
        buildViewModel(
          partnerNumbers = paginationHelper1.partnerDetailsBusinessNumberList,
          userAnswers    = emptyUserAnswers
        )

      viewModel.showSubmitMessage mustBe false
    }
  }
}
