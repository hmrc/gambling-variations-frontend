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

import base.SpecBase
import config.FrontendAppConfig
import pages.partnerdetails.*
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PartnerDetailsViewModelSpec extends SpecBase {

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

  "PartnerDetailsViewModel" - {

    "show no partners message when no partners exist" in {

      val viewModel =
        PartnerDetailsViewModel.from(
          emptyUserAnswers,
          frontendAppConfig
        )

      viewModel.partners mustBe empty
      viewModel.showNoPartnersMessage mustBe true
      viewModel.showSubmitMessage mustBe false
      viewModel.addAnotherPartner mustBe true
      viewModel.showMinimumPartnersMessage mustBe false
      viewModel.showMaximumPartnersMessage mustBe false
    }

    "use MGD registration number when trading and business names are absent" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      viewModel.partners must have size 1
      viewModel.partners.head.name mustBe "XWM00000001762"
    }

    "use trading name when present" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsTradingNamePage(0),
            "XYZ Trading"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(0),
            "XYZ Business"
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      viewModel.partners.head.name mustBe "XYZ Trading"
    }

    "use business name when trading name is absent" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(0),
            "XYZ Business"
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      viewModel.partners.head.name mustBe "XYZ Business"
    }

    "show active status when no future join or leave dates exist" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      val row = viewModel.partners.head

      row.status mustBe messages("partnerDetails.status.active")
      row.statusDetails mustBe None
    }

    "show due to join status when joining date is in the future" in {

      val joinDate = LocalDate.now().plusDays(10)

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfJoiningPage(0),
            joinDate
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      val row = viewModel.partners.head

      row.status mustBe messages("partnerDetails.status.dueToJoin")
      row.statusDetails mustBe defined
      row.statusDetails.value mustBe joinDate.format(dateFormatter)
    }

    "show due to leave status when leaving date is in the future" in {

      val leaveDate = LocalDate.now().plusDays(10)

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(0),
            leaveDate
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      val row = viewModel.partners.head

      row.status mustBe messages("partnerDetails.status.dueToLeave")
      row.statusDetails mustBe defined
      row.statusDetails.value mustBe leaveDate.format(dateFormatter)
    }

    "prioritise due to leave over due to join" in {

      val joiningDate = LocalDate.now().plusDays(20)
      val leavingDate = LocalDate.now().plusDays(10)

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfJoiningPage(0),
            joiningDate
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(0),
            leavingDate
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      viewModel.partners.head.status mustBe
        messages("partnerDetails.status.dueToLeave")

      viewModel.partners.head.statusDetails mustBe
        Some(leavingDate.format(dateFormatter))
    }

    "allow partner removal when leaving date is absent" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      val row = viewModel.partners.head

      row.canRemove mustBe true
      row.removeUrl mustBe defined
    }

    "not allow partner removal when leaving date is present" in {

      val leavingDate = LocalDate.now().plusDays(10)

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(0),
            leavingDate
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      val row = viewModel.partners.head

      row.canRemove mustBe false
      row.removeUrl mustBe None
    }

    "sort partners alphabetically by name" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "2"
          )
          .success
          .value
          .set(
            PartnerDetailsTradingNamePage(0),
            "Zulu"
          )
          .success
          .value
          .set(
            PartnerDetailsPage(1),
            "1"
          )
          .success
          .value
          .set(
            PartnerDetailsTradingNamePage(1),
            "Alpha"
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
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
            PartnerDetailsPage(0),
            "1"
          )
          .success
          .value
          .set(
            PartnerDetailsTradingNamePage(0),
            "zulu"
          )
          .success
          .value
          .set(
            PartnerDetailsPage(1),
            "2"
          )
          .success
          .value
          .set(
            PartnerDetailsTradingNamePage(1),
            "Alpha"
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      viewModel.partners.map(_.name) mustBe Seq(
        "Alpha",
        "zulu"
      )
    }

    "show due to join status when joining date is today" in {

      val joinDate = LocalDate.now()

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfJoiningPage(0),
            joinDate
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      val row = viewModel.partners.head

      row.status mustBe messages("partnerDetails.status.dueToJoin")
      row.statusDetails mustBe None
    }

    "show due to leave status when leaving date is today" in {

      val leavingDate = LocalDate.now()

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(0),
            leavingDate
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      val row = viewModel.partners.head

      row.status mustBe messages("partnerDetails.status.dueToLeave")
      row.statusDetails mustBe None
    }

    "show minimum partners message when fewer than three active partners exist" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      viewModel.partners must have size 1
      viewModel.showMinimumPartnersMessage mustBe true
      viewModel.showSubmitMessage mustBe true
    }

    "not show minimum partners message when there are three active partners" in {

      val answers =
        (0 until 3).foldLeft(emptyUserAnswers) { (userAnswers, index) =>
          userAnswers
            .set(
              PartnerDetailsPage(index),
              s"XWM0000000176$index"
            )
            .success
            .value
        }

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      viewModel.partners must have size 3
      viewModel.showMinimumPartnersMessage mustBe false
      viewModel.showSubmitMessage mustBe true
    }

    "show maximum partners message when maximum number of partners is reached" in {

      val answers =
        (0 until 10).foldLeft(emptyUserAnswers) { (userAnswers, index) =>
          userAnswers
            .set(
              PartnerDetailsPage(index),
              s"XWM0000000${index.toString.reverse.padTo(4, '0').reverse}"
            )
            .success
            .value
        }

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      viewModel.partners must have size 10
      viewModel.showMaximumPartnersMessage mustBe true
      viewModel.addAnotherPartner mustBe false
      viewModel.showSubmitMessage mustBe true
    }

    "allow another partner when below maximum" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      viewModel.addAnotherPartner mustBe true
      viewModel.showMaximumPartnersMessage mustBe false
    }

    "show submit message when at least one partner exists" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            "XWM00000001762"
          )
          .success
          .value

      val viewModel =
        PartnerDetailsViewModel.from(
          answers,
          frontendAppConfig
        )

      viewModel.showSubmitMessage mustBe true
    }

    "not show submit message when there are no partners" in {

      val viewModel =
        PartnerDetailsViewModel.from(
          emptyUserAnswers,
          frontendAppConfig
        )

      viewModel.showSubmitMessage mustBe false
    }
  }
}
