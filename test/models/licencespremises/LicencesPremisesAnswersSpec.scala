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

package models.licencespremises

import base.SpecBase
import pages.licencespremises.*

class LicencesPremisesAnswersSpec extends SpecBase {

  import LicencesPremisesAnswers.*

  "backendFlagOption" - {

    "must convert 1 to true and 0 to false" in {
      emptyUserAnswers.set(ClubLicencePage, "1").success.value.backendFlagOption(ClubLicencePage) mustBe Some(true)
      emptyUserAnswers.set(ClubLicencePage, "0").success.value.backendFlagOption(ClubLicencePage) mustBe Some(false)
    }

    "must be empty when the flag is missing" in {
      emptyUserAnswers.backendFlagOption(ClubLicencePage) mustBe None
    }

    "must throw an exception when the flag is neither 1 nor 0" in {
      an[IllegalArgumentException] mustBe thrownBy(emptyUserAnswers.set(ClubLicencePage, "Y").success.value.backendFlagOption(ClubLicencePage))
    }
  }

  "backendFlag" - {

    "must convert 1 to true and 0 to false" in {
      emptyUserAnswers.set(ClubLicencePage, "1").success.value.backendFlag(ClubLicencePage) mustBe true
      emptyUserAnswers.set(ClubLicencePage, "0").success.value.backendFlag(ClubLicencePage) mustBe false
    }

    "must treat a missing flag as false" in {
      emptyUserAnswers.backendFlag(ClubLicencePage) mustBe false
    }

    "must throw an exception when the flag is neither 1 nor 0" in {
      an[IllegalArgumentException] mustBe thrownBy(emptyUserAnswers.set(ClubLicencePage, "Y").success.value.backendFlag(ClubLicencePage))
    }
  }

  "setBackendFlag" - {

    "must write true as 1 and false as 0" in {
      emptyUserAnswers.setBackendFlag(ClubLicencePage, true).success.value.get(ClubLicencePage) mustBe Some("1")
      emptyUserAnswers.setBackendFlag(ClubLicencePage, false).success.value.get(ClubLicencePage) mustBe Some("0")
    }

    "must round trip through backendFlag" in {
      emptyUserAnswers.setBackendFlag(ClubLicencePage, true).success.value.backendFlag(ClubLicencePage) mustBe true
      emptyUserAnswers.setBackendFlag(ClubLicencePage, false).success.value.backendFlag(ClubLicencePage) mustBe false
    }
  }

  "pubTenantAnswer" - {

    "must be No when there is no answer" in {
      emptyUserAnswers.pubTenantAnswer mustBe false
    }

    "must read the backend flag" in {
      emptyUserAnswers.set(LicenceHeldByLandlordPage, "1").success.value.pubTenantAnswer mustBe true
      emptyUserAnswers.set(LicenceHeldByLandlordPage, "0").success.value.pubTenantAnswer mustBe false
    }
  }

  "premisesNotCoveredAnswer" - {

    "must be No when there is no answer" in {
      emptyUserAnswers.premisesNotCoveredAnswer mustBe false
    }

    "must read the backend flag" in {
      emptyUserAnswers.set(LicencePremisesNotCoveredPage, "1").success.value.premisesNotCoveredAnswer mustBe true
      emptyUserAnswers.set(LicencePremisesNotCoveredPage, "0").success.value.premisesNotCoveredAnswer mustBe false
    }
  }

  "hasLicencesOrPermits" - {

    "must be false when nothing has been provided" in {
      emptyUserAnswers.hasLicencesOrPermits mustBe false
    }

    "must treat a blank licence number as not provided" in {
      emptyUserAnswers.set(LicenceNumberPage, " ").success.value.hasLicencesOrPermits mustBe false
    }

    "must be true when any single licence or permit has been provided" in {
      Seq(
        emptyUserAnswers.set(LicenceNumberPage, "123").success.value,
        emptyUserAnswers.set(LicenceHeldByLandlordPage, "1").success.value,
        emptyUserAnswers.set(ClubLicencePage, "1").success.value,
        emptyUserAnswers.set(LicenceBingoPage, "1").success.value
      ).foreach(_.hasLicencesOrPermits mustBe true)
    }

    "must ignore the no other licences flags and flags set to 0" in {
      val answers = emptyUserAnswers
        .set(NoOtherLicencesAndPermitsGBPage, "1")
        .success
        .value
        .set(NoOtherLicencesAndPermitsNIPage, "1")
        .success
        .value
        .set(ClubLicencePage, "0")
        .success
        .value

      answers.hasLicencesOrPermits mustBe false
    }
  }

  "premisesCount" - {

    "must count the premises rather than use the total" in {
      val premises = PremisesDetails("id", Some("1 Street"), None, None, None, Some("AA1 1AA"), None)
      val answers = emptyUserAnswers.set(PremisesDetailsPage, PremisesDetailsResponse(Some(1000), Seq(premises, premises))).success.value

      answers.premisesCount mustBe 2
    }

    "must be zero when there are no premises details" in {
      emptyUserAnswers.premisesCount mustBe 0
    }
  }

  "provideAddressesAnswer" - {

    "must use the stored answer when the question has been answered" in {
      val premises = PremisesDetails("id", Some("1 Street"), None, None, None, Some("AA1 1AA"), None)
      val answers = emptyUserAnswers
        .set(PremisesDetailsPage, PremisesDetailsResponse(Some(1), Seq(premises)))
        .success
        .value
        .set(LicencesPremisesPage, LicencesAndPremisesRadioOptions.ByPost)
        .success
        .value

      answers.provideAddressesAnswer mustBe LicencesAndPremisesRadioOptions.ByPost
    }

    "must derive online from the premises when the question has not been answered" in {
      val premises = PremisesDetails("id", Some("1 Street"), None, None, None, Some("AA1 1AA"), None)
      val answers = emptyUserAnswers.set(PremisesDetailsPage, PremisesDetailsResponse(Some(1), Seq(premises))).success.value

      answers.provideAddressesAnswer mustBe LicencesAndPremisesRadioOptions.Online
    }

    "must derive by post when the question has not been answered and there are no premises" in {
      emptyUserAnswers.provideAddressesAnswer mustBe LicencesAndPremisesRadioOptions.ByPost
    }
  }

  "withLicencesPremisesFlags" - {

    "must set submitted, and changed only when the answer is changed" in {
      val unchanged = emptyUserAnswers.withLicencesPremisesFlags(isChanged = false).success.value
      unchanged.get(LicencesPremisesDetailsSubmittedPage) mustBe Some(true)
      unchanged.get(LicencesPremisesDetailsChangesPage) mustBe Some(false)

      emptyUserAnswers.withLicencesPremisesFlags(isChanged = true).success.value.get(LicencesPremisesDetailsChangesPage) mustBe Some(true)
    }

    "must keep the section changed once it has been flagged as changed" in {
      val flagged = emptyUserAnswers.set(LicencesPremisesDetailsChangesPage, true).success.value

      flagged.withLicencesPremisesFlags(isChanged = false).success.value.get(LicencesPremisesDetailsChangesPage) mustBe Some(true)
    }
  }
}
