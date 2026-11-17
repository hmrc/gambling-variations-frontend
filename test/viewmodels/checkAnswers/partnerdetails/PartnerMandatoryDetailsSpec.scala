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
import models.{ModifiedAnswers, UserAnswers}
import pages.BusinessNumberOrIndex
import pages.partnerdetails.*
import viewmodels.checkAnswers.partnerdetails.PartnerMandatoryDetails.sharedMandatoryFields

import java.time.LocalDate
import scala.util.Try

class PartnerMandatoryDetailsSpec extends SpecBase with PartnerDetailsHelper {

  private val idx: BusinessNumberOrIndex = newPartnersIndex1

  "PartnerMandatoryDetails" - {

    "cleanUp - new partners only" - {

      "soleproprietor" in {
        val businessData = cleanedDataNewPartners(
          nino                   = Some(testNino),
          vrn                    = Some(testVRN),
          utr                    = Some(testUtr),
          fcr                    = Some(testFCR),
          crn                    = Some(testCRN),
          dateOfIncorporation    = Some("2000-05-05"),
          countryOfIncorporation = Some("France"),
          isIncorporatedInTheUK  = Some(true),
          hasAnsweredAllQuestion = Some(true),
          soleProprietorDob      = testSolePropDOB,
          soleProprietorDetails  = testSoleProprietor
        )
        val userAnswers: UserAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        userAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe Some(true)
        userAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe Some(LocalDate.of(2000, 5, 5))
        userAnswers.get(PartnerDetailsCrnPage(idx)) mustBe Some(testCRN)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some("France")
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe None

      }

      "corporatebody - uk" in {
        val businessData = cleanedDataNewPartners(
          nino                   = Some(testNino),
          vrn                    = Some(testVRN),
          utr                    = Some(testUtr),
          fcr                    = Some(testFCR),
          crn                    = Some(testCRN),
          dateOfIncorporation    = Some("2000-05-05"),
          countryOfIncorporation = Some("France"),
          isIncorporatedInTheUK  = Some(true),
          hasAnsweredAllQuestion = Some(true),
          soleProprietorDob      = testSolePropDOB,
          soleProprietorDetails  = testSoleProprietor,
          businessType           = 2
        )
        val userAnswers: UserAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        userAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe Some(true)
        userAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe Some(LocalDate.of(2000, 5, 5))
        userAnswers.get(PartnerDetailsCrnPage(idx)) mustBe Some(testCRN)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some("France")
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe None

      }

      "corporatebody - non uk" in {
        val businessData = cleanedDataNewPartners(
          nino                   = Some(testNino),
          vrn                    = Some(testVRN),
          utr                    = Some(testUtr),
          fcr                    = Some(testFCR),
          crn                    = Some(testCRN),
          dateOfIncorporation    = Some("2000-05-05"),
          countryOfIncorporation = Some("France"),
          isIncorporatedInTheUK  = Some(false),
          hasAnsweredAllQuestion = Some(true),
          soleProprietorDob      = testSolePropDOB,
          soleProprietorDetails  = testSoleProprietor,
          businessType           = 2
        )
        val userAnswers: UserAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        userAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe Some(true)
        userAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe Some(LocalDate.of(2000, 5, 5))
        userAnswers.get(PartnerDetailsCrnPage(idx)) mustBe Some(testCRN)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some("France")
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe None

      }

      "unincorporatedbody" in {
        val businessData = cleanedDataNewPartners(
          nino                   = Some(testNino),
          vrn                    = Some(testVRN),
          utr                    = Some(testUtr),
          fcr                    = Some(testFCR),
          crn                    = Some(testCRN),
          dateOfIncorporation    = Some("2000-05-05"),
          countryOfIncorporation = Some("France"),
          isIncorporatedInTheUK  = Some(true),
          hasAnsweredAllQuestion = Some(true),
          soleProprietorDob      = testSolePropDOB,
          soleProprietorDetails  = testSoleProprietor,
          businessType           = 3
        )
        val userAnswers: UserAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        userAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe Some(true)
        userAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe Some(LocalDate.of(2000, 5, 5))
        userAnswers.get(PartnerDetailsCrnPage(idx)) mustBe Some(testCRN)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some("France")
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe None

      }

      "partnership" in {
        val businessData = cleanedDataNewPartners(
          nino                   = Some(testNino),
          vrn                    = Some(testVRN),
          utr                    = Some(testUtr),
          fcr                    = Some(testFCR),
          crn                    = Some(testCRN),
          dateOfIncorporation    = Some("2000-05-05"),
          countryOfIncorporation = Some("France"),
          isIncorporatedInTheUK  = Some(true),
          hasAnsweredAllQuestion = Some(true),
          soleProprietorDob      = testSolePropDOB,
          soleProprietorDetails  = testSoleProprietor,
          businessType           = 4
        )
        val userAnswers: UserAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        userAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe Some(true)
        userAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe Some(LocalDate.of(2000, 5, 5))
        userAnswers.get(PartnerDetailsCrnPage(idx)) mustBe Some(testCRN)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some("France")
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe None

      }

      "llp" in {
        val businessData = cleanedDataNewPartners(
          nino                   = Some(testNino),
          vrn                    = Some(testVRN),
          utr                    = Some(testUtr),
          fcr                    = Some(testFCR),
          crn                    = Some(testCRN),
          dateOfIncorporation    = Some("2000-05-05"),
          countryOfIncorporation = Some("France"),
          isIncorporatedInTheUK  = Some(true),
          hasAnsweredAllQuestion = Some(true),
          soleProprietorDob      = testSolePropDOB,
          soleProprietorDetails  = testSoleProprietor,
          businessType           = 5
        )
        val userAnswers: UserAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        userAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe Some("XYZ Consulting Ltd")
        userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe Some(true)
        userAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe Some(LocalDate.of(2000, 5, 5))
        userAnswers.get(PartnerDetailsCrnPage(idx)) mustBe Some(testCRN)
        userAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe Some("France")
        userAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe Some(testFCR)

        val cleanedUpAnswers: UserAnswers = PartnerMandatoryDetails.cleanUp(userAnswers, idx).success.value

        cleanedUpAnswers.get(PartnerDetailsBusinessNamePage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsDateOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCrnPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsCountryOfIncorporationPage(idx)) mustBe None
        cleanedUpAnswers.get(PartnerDetailsForeignCorporateReferencePage(idx)) mustBe None

      }

    }

    "required" - {

      val shared = sharedMandatoryFields(idx).map(_.page.toString)

      "soleproprietor" in {
        val businessData = cleanedDataNewPartners()
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        val expected =
          Seq("partnerDetailsBusinessType", "partnerDetailsSoleProprietor", "partnerDetailsDateOfBirth", "partnerDetailsUtr").concat(shared)

        PartnerMandatoryDetails.required(userAnswers, newPartnersIndex1).map(_.page.toString) mustBe expected
      }

      "corporatebody - uk" in {

        val businessData = cleanedDataNewPartners(businessType = 2, isIncorporatedInTheUK = Some(true))
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

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
        val businessData = cleanedDataNewPartners(businessType = 2, isIncorporatedInTheUK = Some(false))
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

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

        val businessData = cleanedDataNewPartners(businessType = 3)
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        val expected =
          Seq("partnerDetailsBusinessType", "partnerDetailsBusinessName", "partnerDetailsUtr").concat(shared)

        PartnerMandatoryDetails.required(userAnswers, newPartnersIndex1).map(_.page.toString) mustBe expected
      }

      "partnership" in {
        val businessData = cleanedDataNewPartners(businessType = 4)
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        val expected =
          Seq("partnerDetailsBusinessType", "partnerDetailsBusinessName").concat(shared)

        PartnerMandatoryDetails.required(userAnswers, newPartnersIndex1).map(_.page.toString) mustBe expected
      }

      "llp" in {
        val businessData = cleanedDataNewPartners(businessType = 5)
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

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
