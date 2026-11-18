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
import controllers.partnerdetails.PartnerDetailsHelper
import models.BusinessType.{Corporatebody, LimitedLiabilityPartnership, Partnership, Soleproprietor, Unincorporatedbody}
import models.{ContactNumber, ModifiedAnswers, SoleProprietorName, UserAnswers}
import pages.BusinessNumberOrIndex
import pages.partnerdetails.*
import play.api.libs.json.Reads
import viewmodels.checkAnswers.partnerdetails.PartnerMandatoryDetails.sharedMandatoryFields

import java.time.LocalDate

class PartnerMandatoryDetailsSpec extends SpecBase with PartnerDetailsHelper {

  private val idx: BusinessNumberOrIndex = newPartnersIndex1

  private def answersFor(businessType: Int = 1, isUk: Boolean = true): UserAnswers =
    UserAnswers(
      id = mgdRegNumber,
      modified = ModifiedAnswers(
        cleanedDataNewPartners(
          phoneNumber            = Some(testPhoneNumber),
          nino                   = Some(testNino),
          vrn                    = Some(testVRN),
          utr                    = Some(testUtr),
          fcr                    = Some(testFCR),
          crn                    = Some(testCRN),
          dateOfIncorporation    = Some(testDateOfIncorporationAsString),
          countryOfIncorporation = Some(testCountryOfIncorporation),
          isIncorporatedInTheUK  = Some(isUk),
          hasAnsweredAllQuestion = Some(true),
          soleProprietorDob      = testSolePropDOB,
          soleProprietorDetails  = testSoleProprietor,
          businessType           = businessType
        )
      )
    )

  "PartnerMandatoryDetails" - {

    "cleanUp - new partners only" - {

      "soleproprietor" in {
        val userAnswers: UserAnswers = answersFor()

        userAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe Some(true)
        userAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe Some(testDateOfIncorporation)
        userAnswers.get(PartnerDetailsCrnPage(idx)) mustBe Some(testCRN)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some(testCountryOfIncorporation)
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)
        userAnswers.get(PartnerDetailsVrnPage(idx)) mustBe Some(testVRN)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe None

        cleanedUpAnswers.get(PartnerDetailsContactNumberPage(idx)) mustBe Some(ContactNumber(Some(testPhoneNumber), None))
        cleanedUpAnswers.get(PartnerDetailsDateOfJoiningPage(idx)) mustBe Some(testDateOfJoining)
        cleanedUpAnswers.get(PartnerDetailsUtrPage(idx)) mustBe Some(testUtr)
        cleanedUpAnswers.get(PartnerDetailsBusinessTypePage(idx)) mustBe Some(Soleproprietor)
        cleanedUpAnswers.get(PartnerDetailsDateOfBirthPage(idx)) mustBe Some(testDateSolePropDOB)
        cleanedUpAnswers.get(PartnerDetailsVrnPage(idx)) mustBe Some(testVRN)
        cleanedUpAnswers.get(PartnerDetailsSoleProprietorPage(idx)) mustBe Some(testSoleProprietorADT)

      }

      "corporatebody - uk" in {
        val userAnswers: UserAnswers = answersFor(2)

        userAnswers.get(PartnerDetailsSoleProprietorPage(idx)) mustBe Some(testSoleProprietorADT)
        userAnswers.get(PartnerDetailsDateOfBirthPage(idx)) mustBe Some(testDateSolePropDOB)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some(testCountryOfIncorporation)
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsSoleProprietorPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfBirthPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe None

        cleanedUpAnswers.get(PartnerDetailsBusinessTypePage(idx)) mustBe Some(Corporatebody)
        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        cleanedUpAnswers.get(PartnerDetailsDateOfJoiningPage(idx)) mustBe Some(testDateOfJoining)
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe Some(true)
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe Some(testDateOfIncorporation)
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe Some(testCRN)
        cleanedUpAnswers.get(PartnerDetailsUtrPage(idx)) mustBe Some(testUtr)
        cleanedUpAnswers.get(PartnerDetailsVrnPage(idx)) mustBe Some(testVRN)
        cleanedUpAnswers.get(PartnerDetailsContactNumberPage(idx)) mustBe Some(ContactNumber(Some(testPhoneNumber), None))

      }

      "corporatebody - non uk" in {
        val userAnswers: UserAnswers = answersFor(2, false)

        userAnswers.get(PartnerDetailsSoleProprietorPage(idx)) mustBe Some(testSoleProprietorADT)
        userAnswers.get(PartnerDetailsDateOfBirthPage(idx)) mustBe Some(testDateSolePropDOB)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some(testCountryOfIncorporation)
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)
        userAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe Some(testDateOfIncorporation)
        userAnswers.get(PartnerDetailsCrnPage(idx)) mustBe Some(testCRN)
        userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe Some(false)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsSoleProprietorPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfBirthPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe None

        cleanedUpAnswers.get(PartnerDetailsBusinessTypePage(idx)) mustBe Some(Corporatebody)
        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        cleanedUpAnswers.get(PartnerDetailsDateOfJoiningPage(idx)) mustBe Some(testDateOfJoining)
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe Some(false)
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some(testCountryOfIncorporation)
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)
        cleanedUpAnswers.get(PartnerDetailsUtrPage(idx)) mustBe Some(testUtr)
        cleanedUpAnswers.get(PartnerDetailsVrnPage(idx)) mustBe Some(testVRN)
        cleanedUpAnswers.get(PartnerDetailsContactNumberPage(idx)) mustBe Some(ContactNumber(Some(testPhoneNumber), None))

      }

      "unincorporatedbody" in {
        val userAnswers: UserAnswers = answersFor(3)

        userAnswers.get(PartnerDetailsSoleProprietorPage(idx)) mustBe Some(testSoleProprietorADT)
        userAnswers.get(PartnerDetailsDateOfBirthPage(idx)) mustBe Some(testDateSolePropDOB)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some(testCountryOfIncorporation)
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsSoleProprietorPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfBirthPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe None

        cleanedUpAnswers.get(PartnerDetailsBusinessTypePage(idx)) mustBe Some(Unincorporatedbody)
        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        cleanedUpAnswers.get(PartnerDetailsDateOfJoiningPage(idx)) mustBe Some(testDateOfJoining)
        cleanedUpAnswers.get(PartnerDetailsUtrPage(idx)) mustBe Some(testUtr)
        cleanedUpAnswers.get(PartnerDetailsVrnPage(idx)) mustBe Some(testVRN)
        cleanedUpAnswers.get(PartnerDetailsContactNumberPage(idx)) mustBe Some(ContactNumber(Some(testPhoneNumber), None))

      }

      "partnership" in {
        val userAnswers: UserAnswers = answersFor(4)

        userAnswers.get(PartnerDetailsSoleProprietorPage(idx)) mustBe Some(testSoleProprietorADT)
        userAnswers.get(PartnerDetailsDateOfBirthPage(idx)) mustBe Some(testDateSolePropDOB)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some(testCountryOfIncorporation)
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsSoleProprietorPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfBirthPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsUtrPage(idx)) mustBe None

        cleanedUpAnswers.get(PartnerDetailsBusinessTypePage(idx)) mustBe Some(Partnership)
        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        cleanedUpAnswers.get(PartnerDetailsDateOfJoiningPage(idx)) mustBe Some(testDateOfJoining)
        cleanedUpAnswers.get(PartnerDetailsVrnPage(idx)) mustBe Some(testVRN)
        cleanedUpAnswers.get(PartnerDetailsContactNumberPage(idx)) mustBe Some(ContactNumber(Some(testPhoneNumber), None))

      }

      "llp" in {
        val userAnswers: UserAnswers = answersFor(5)

        userAnswers.get(PartnerDetailsSoleProprietorPage(idx)) mustBe Some(testSoleProprietorADT)
        userAnswers.get(PartnerDetailsDateOfBirthPage(idx)) mustBe Some(testDateSolePropDOB)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some(testCountryOfIncorporation)
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsSoleProprietorPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfBirthPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe None

        cleanedUpAnswers.get(PartnerDetailsBusinessTypePage(idx)) mustBe Some(LimitedLiabilityPartnership)
        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        cleanedUpAnswers.get(PartnerDetailsDateOfJoiningPage(idx)) mustBe Some(testDateOfJoining)
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe Some(testDateOfIncorporation)
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe Some(testCRN)
        cleanedUpAnswers.get(PartnerDetailsUtrPage(idx)) mustBe Some(testUtr)
        cleanedUpAnswers.get(PartnerDetailsVrnPage(idx)) mustBe Some(testVRN)
        cleanedUpAnswers.get(PartnerDetailsContactNumberPage(idx)) mustBe Some(ContactNumber(Some(testPhoneNumber), None))

      }

    }

    "required" - {

      val shared = sharedMandatoryFields(idx).map(_.page.toString)

      "soleproprietor" in {
        val userAnswers = answersFor()

        val expected =
          Seq("partnerDetailsBusinessType", "partnerDetailsSoleProprietor", "partnerDetailsDateOfBirth", "partnerDetailsUtr").concat(shared)

        PartnerMandatoryDetails.required(userAnswers, newPartnersIndex1).map(_.page.toString) mustBe expected
      }

      "corporatebody - uk" in {

        val userAnswers = answersFor(2)

        val expected =
          Seq(
            "partnerDetailsBusinessType",
            "partnerDetailsBusinessName",
            "partnerDetailsIsBusinessIncorporatedUk",
            "partnerDetailsDateOfIncorporation",
            "partnerDetailsCrn",
            "partnerDetailsUtr"
          ).concat(shared)

        PartnerMandatoryDetails.required(userAnswers, newPartnersIndex1).map(_.page.toString) mustBe expected

      }

      "corporatebody - non uk" in {
        val userAnswers = answersFor(2, false)

        val expected =
          Seq(
            "partnerDetailsBusinessType",
            "partnerDetailsBusinessName",
            "partnerDetailsIsBusinessIncorporatedUk",
            "partnerDetailsCountryOfIncorporation",
            "partnerDetailsForeignCorporateRef",
            "partnerDetailsUtr"
          ).concat(shared)

        PartnerMandatoryDetails.required(userAnswers, newPartnersIndex1).map(_.page.toString) mustBe expected
      }

      "unincorporatedbody" in {

        val userAnswers = answersFor(3)

        val expected =
          Seq("partnerDetailsBusinessType", "partnerDetailsBusinessName", "partnerDetailsUtr").concat(shared)

        PartnerMandatoryDetails.required(userAnswers, newPartnersIndex1).map(_.page.toString) mustBe expected
      }

      "partnership" in {
        val userAnswers = answersFor(4)

        val expected =
          Seq("partnerDetailsBusinessType", "partnerDetailsBusinessName").concat(shared)

        PartnerMandatoryDetails.required(userAnswers, newPartnersIndex1).map(_.page.toString) mustBe expected
      }

      "llp" in {
        val userAnswers = answersFor(5)

        val expected =
          Seq("partnerDetailsBusinessType",
              "partnerDetailsBusinessName",
              "partnerDetailsDateOfIncorporation",
              "partnerDetailsCrn",
              "partnerDetailsUtr"
             ).concat(shared)

        PartnerMandatoryDetails.required(userAnswers, newPartnersIndex1).map(_.page.toString) mustBe expected
      }
    }

  }
}
