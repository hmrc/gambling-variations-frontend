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

package navigation

import base.SpecBase
import controllers.controllingbody.routes as controllingBodyRoutes
import controllers.routes
import models.BusinessType.{Corporatebody, LimitedLiabilityPartnership, Partnership, Soleproprietor, Unincorporatedbody}
import models.{BusinessType, CheckMode, NormalMode, UserAnswers}
import pages.controllingbody.*

class ControllingBodyTypeChangeNavigatorSpec extends SpecBase {

  private val navigator = new Navigator

  private def answers(current: BusinessType, previous: Option[BusinessType]): UserAnswers =
    UserAnswers("id")
      .set(ControllingBodyBusinessPartnerNumberPage, "1234567890")
      .success
      .value
      .set(ControllingBodyBusinessTypePage, current)
      .success
      .value
      .setIfDefined(ControllingBodyPreviousBusinessTypePage, previous)
      .success
      .value

  "Navigator, when an existing controlling body is changed to a limited liability partnership" - {

    Seq(Soleproprietor, Unincorporatedbody, Corporatebody, Partnership).foreach { previous =>
      s"from $previous" - {

        val changed = answers(LimitedLiabilityPartnership, Some(previous))

        "must route CB-BN-LLP to CB-DIN" in {
          navigator.nextPage(ControllingBodyBusinessNamePage, NormalMode, changed) mustBe
            controllingBodyRoutes.DateOfIncorporationController.onPageLoad()
        }

        "must route CB-DIN to CB-CR" in {
          navigator.nextPage(ControllingBodyDateOfIncorporationPage, NormalMode, changed) mustBe
            controllingBodyRoutes.ControllingBodyCompanyRegistrationNumberController.onPageLoad()
        }
      }
    }

    "from Partnership" - {

      val changed = answers(LimitedLiabilityPartnership, Some(Partnership))

      "must route CB-CR to CB-UTR" in {
        navigator.nextPage(ControllingBodyCrnPage, NormalMode, changed) mustBe
          controllingBodyRoutes.ControllingBodyUTRController.onPageLoad()
      }

      "must route CB-UTR to CB-CYA" in {
        navigator.nextPage(ControllingBodyUtrPage, NormalMode, changed) mustBe
          controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
      }
    }

    Seq(Soleproprietor, Unincorporatedbody, Corporatebody).foreach { previous =>
      s"from $previous" - {

        val changed = answers(LimitedLiabilityPartnership, Some(previous))

        "must route CB-CR to CB-CYA" in {
          navigator.nextPage(ControllingBodyCrnPage, NormalMode, changed) mustBe
            controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
        }

        "must keep the normal route from CB-UTR" in {
          navigator.nextPage(ControllingBodyUtrPage, NormalMode, changed) mustBe
            controllingBodyRoutes.ControllingBodyVATScreenerController.onPageLoad()
        }
      }
    }

    "must keep the normal routes of the pages outside the mini-flow" in {
      val changed = answers(LimitedLiabilityPartnership, Some(Partnership))

      navigator.nextPage(ControllingBodyEmailPage, NormalMode, changed) mustBe
        controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
      navigator.nextPage(ControllingBodyBusinessTypePage, NormalMode, changed) mustBe
        controllingBodyRoutes.ChangeControllingBodyNameController.onPageLoad(LimitedLiabilityPartnership)
    }

    "must not change the routes in check mode" in {
      val changed = answers(LimitedLiabilityPartnership, Some(Partnership))

      navigator.nextPage(ControllingBodyBusinessNamePage, CheckMode, changed) mustBe
        routes.ChangeRegistrationDetailsController.onPageLoad()
    }
  }

  "Navigator, when an existing controlling body is changed to an unincorporated body" - {

    "from Partnership" - {

      val changed = answers(Unincorporatedbody, Some(Partnership))

      "must route CB-BN-UB to CB-UTR" in {
        navigator.nextPage(ControllingBodyBusinessNamePage, NormalMode, changed) mustBe
          controllingBodyRoutes.ControllingBodyUTRController.onPageLoad()
      }

      "must route CB-UTR to CB-CYA" in {
        navigator.nextPage(ControllingBodyUtrPage, NormalMode, changed) mustBe
          controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
      }
    }

    Seq(Soleproprietor, LimitedLiabilityPartnership, Corporatebody).foreach { previous =>
      s"from $previous" - {

        val changed = answers(Unincorporatedbody, Some(previous))

        "must route CB-BN-UB to CB-CYA" in {
          navigator.nextPage(ControllingBodyBusinessNamePage, NormalMode, changed) mustBe
            controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
        }

        "must keep the normal route from CB-UTR" in {
          navigator.nextPage(ControllingBodyUtrPage, NormalMode, changed) mustBe
            controllingBodyRoutes.ControllingBodyVATScreenerController.onPageLoad()
        }
      }
    }

    "must keep the normal routes of the pages outside the mini-flow" in {
      val changed = answers(Unincorporatedbody, Some(Partnership))

      navigator.nextPage(ControllingBodyEmailPage, NormalMode, changed) mustBe
        controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
      navigator.nextPage(ControllingBodyBusinessTypePage, NormalMode, changed) mustBe
        controllingBodyRoutes.ChangeControllingBodyNameController.onPageLoad(Unincorporatedbody)
    }

    "must not change the routes in check mode" in {
      val changed = answers(Unincorporatedbody, Some(Partnership))

      navigator.nextPage(ControllingBodyBusinessNamePage, CheckMode, changed) mustBe
        routes.ChangeRegistrationDetailsController.onPageLoad()
    }
  }

  "Navigator, when the controlling body has not been changed to an unincorporated body" - {

    "must keep the normal routes for an existing unincorporated body" in {
      val unchanged = answers(Unincorporatedbody, None)

      navigator.nextPage(ControllingBodyBusinessNamePage, NormalMode, unchanged) mustBe
        controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
      navigator.nextPage(ControllingBodyUtrPage, NormalMode, unchanged) mustBe
        controllingBodyRoutes.ControllingBodyVATScreenerController.onPageLoad()
    }
  }

  "Navigator, when the controlling body has not been changed to a limited liability partnership" - {

    "must keep the normal routes for an existing limited liability partnership" in {
      val unchanged = answers(LimitedLiabilityPartnership, None)

      navigator.nextPage(ControllingBodyBusinessNamePage, NormalMode, unchanged) mustBe
        controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
      navigator.nextPage(ControllingBodyDateOfIncorporationPage, NormalMode, unchanged) mustBe
        controllingBodyRoutes.ControllingBodyCompanyRegistrationNumberController.onPageLoad()
      navigator.nextPage(ControllingBodyUtrPage, NormalMode, unchanged) mustBe
        controllingBodyRoutes.ControllingBodyVATScreenerController.onPageLoad()
    }

    "must keep the normal routes when the type was changed back to the previous one" in {
      val changedBack = answers(Partnership, Some(Partnership))

      navigator.nextPage(ControllingBodyBusinessNamePage, NormalMode, changedBack) mustBe
        controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
      navigator.nextPage(ControllingBodyUtrPage, NormalMode, changedBack) mustBe
        controllingBodyRoutes.ControllingBodyVATScreenerController.onPageLoad()
    }

    "must keep the normal routes when the type was changed to another type" in {
      val changedToCorporateBody = answers(Corporatebody, Some(Partnership))

      navigator.nextPage(ControllingBodyBusinessNamePage, NormalMode, changedToCorporateBody) mustBe
        controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
      navigator.nextPage(ControllingBodyDateOfIncorporationPage, NormalMode, changedToCorporateBody) mustBe
        controllingBodyRoutes.ControllingBodyCompanyRegistrationNumberController.onPageLoad()
    }
  }

  "Navigator, when an existing controlling body is changed to a sole proprietor" - {

    "from Partnership" - {

      val changed = answers(Soleproprietor, Some(Partnership))

      "must route CB-DOB to CB-NI-SC" in {
        navigator.nextPage(ControllingBodyDateOfBirthPage, NormalMode, changed) mustBe
          controllingBodyRoutes.ControllingBodyAddNINumberYesNoController.onPageLoad()
      }

      "must route CB-NI-SC to CB-NI when Yes is selected" in {
        val answersWithYes = changed
          .set(ControllingBodyAddNINumberYesNoPage, true)
          .success
          .value

        navigator.nextPage(ControllingBodyAddNINumberYesNoPage, NormalMode, answersWithYes) mustBe
          controllingBodyRoutes.ControllingBodyNINumberController.onPageLoad()
      }

      "must route CB-NI-SC to CB-UTR when No is selected" in {
        val answersWithNo = changed
          .set(ControllingBodyAddNINumberYesNoPage, false)
          .success
          .value

        navigator.nextPage(ControllingBodyAddNINumberYesNoPage, NormalMode, answersWithNo) mustBe
          controllingBodyRoutes.ControllingBodyUTRController.onPageLoad()
      }

      "must route CB-NI to CB-UTR" in {
        navigator.nextPage(ControllingBodyNinoPage, NormalMode, changed) mustBe
          controllingBodyRoutes.ControllingBodyUTRController.onPageLoad()
      }

      "must route CB-UTR to CB-CYA" in {
        navigator.nextPage(ControllingBodyUtrPage, NormalMode, changed) mustBe
          controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
      }
    }

    Seq(Unincorporatedbody, Corporatebody, LimitedLiabilityPartnership).foreach { previous =>
      s"from $previous" - {

        val changed = answers(Soleproprietor, Some(previous))

        "must route CB-DOB to CB-NI-SC" in {
          navigator.nextPage(ControllingBodyDateOfBirthPage, NormalMode, changed) mustBe
            controllingBodyRoutes.ControllingBodyAddNINumberYesNoController.onPageLoad()
        }

        "must route CB-NI-SC to CB-NI when Yes is selected" in {
          val answersWithYes = changed
            .set(ControllingBodyAddNINumberYesNoPage, true)
            .success
            .value

          navigator.nextPage(ControllingBodyAddNINumberYesNoPage, NormalMode, answersWithYes) mustBe
            controllingBodyRoutes.ControllingBodyNINumberController.onPageLoad()
        }

        "must route CB-NI-SC to CB-CYA when No is selected" in {
          val answersWithNo = changed
            .set(ControllingBodyAddNINumberYesNoPage, false)
            .success
            .value

          navigator.nextPage(ControllingBodyAddNINumberYesNoPage, NormalMode, answersWithNo) mustBe
            controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
        }

        "must route CB-NI to CB-CYA" in {
          navigator.nextPage(ControllingBodyNinoPage, NormalMode, changed) mustBe
            controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
        }
      }
    }

    "must keep the normal routes of pages outside the mini-flow" in {
      val changed = answers(Soleproprietor, Some(Partnership))

      navigator.nextPage(ControllingBodyEmailPage, NormalMode, changed) mustBe
        controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()

      navigator.nextPage(ControllingBodyBusinessTypePage, NormalMode, changed) mustBe
        controllingBodyRoutes.ChangeControllingBodyNameController.onPageLoad(Soleproprietor)
    }

    "must not change the routes in check mode" in {
      val changed = answers(Soleproprietor, Some(Partnership))

      navigator.nextPage(ControllingBodyBusinessNamePage, CheckMode, changed) mustBe
        routes.ChangeRegistrationDetailsController.onPageLoad()
    }

    "must keep the normal route from CB-UTR when previous type was not Partnership" in {
      val changed = answers(Soleproprietor, Some(Corporatebody))

      navigator.nextPage(ControllingBodyUtrPage, NormalMode, changed) mustBe
        controllingBodyRoutes.ControllingBodyVATScreenerController.onPageLoad()
    }
  }

}
