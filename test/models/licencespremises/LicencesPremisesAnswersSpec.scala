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

  "pubTenantAnswer" - {

    "must be No when neither the session nor the backend has an answer" in {
      emptyUserAnswers.pubTenantAnswer mustBe false
    }

    "must use the backend flag when there is no answer from this session" in {
      emptyUserAnswers.set(LicenceHeldByLandlordPage, "1").success.value.pubTenantAnswer mustBe true
      emptyUserAnswers.set(LicenceHeldByLandlordPage, "0").success.value.pubTenantAnswer mustBe false
    }

    "must prefer the answer from this session over the backend flag" in {
      val answers = emptyUserAnswers
        .set(LicenceHeldByLandlordPage, "1")
        .success
        .value
        .set(LicenceDetailsLandlordLicenceYesNoPage, false)
        .success
        .value

      answers.pubTenantAnswer mustBe false
    }
  }

  "premisesNotCoveredAnswer" - {

    "must be No when neither the session nor the backend has an answer" in {
      emptyUserAnswers.premisesNotCoveredAnswer mustBe false
    }

    "must use the backend flag when there is no answer from this session" in {
      emptyUserAnswers.set(LicencePremisesNotCoveredPage, "1").success.value.premisesNotCoveredAnswer mustBe true
    }

    "must prefer the answer from this session over the backend flag" in {
      val answers = emptyUserAnswers
        .set(LicencePremisesNotCoveredPage, "0")
        .success
        .value
        .set(PremisesNotCoveredYesNoPage, true)
        .success
        .value

      answers.premisesNotCoveredAnswer mustBe true
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
