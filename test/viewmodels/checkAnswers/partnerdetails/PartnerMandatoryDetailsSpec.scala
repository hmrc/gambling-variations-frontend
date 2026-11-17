package viewmodels.checkAnswers.partnerdetails

import base.SpecBase
import controllers.partnerdetails.PartnerDetailsHelper
import models.{ModifiedAnswers, UserAnswers}
import pages.BusinessNumberOrIndex
import viewmodels.checkAnswers.partnerdetails.PartnerMandatoryDetails.sharedMandatoryFields

class PartnerMandatoryDetailsSpec extends SpecBase with PartnerDetailsHelper {

  "PartnerMandatoryDetails" - {

    "cleanUp - new partner only" ignore {
      "soleproprietor" in {
        val businessData = cleanedDataNewPartners()
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        PartnerMandatoryDetails.cleanUp(userAnswers, newPartnersIndex1)
      }
      "corporatebody - uk" in {

        val businessData = cleanedDataNewPartners(businessType = 2)
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        PartnerMandatoryDetails.cleanUp(userAnswers, newPartnersIndex1)
      }
      "corporatebody - non uk" in {
        val businessData = cleanedDataNewPartners(businessType = 2)
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        PartnerMandatoryDetails.cleanUp(userAnswers, newPartnersIndex1)
      }
      "unincorporatedbody" in {

        val businessData = cleanedDataNewPartners(businessType = 3)
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        PartnerMandatoryDetails.cleanUp(userAnswers, newPartnersIndex1)
      }
      "partnership" in {
        val businessData = cleanedDataNewPartners(businessType = 4)
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        PartnerMandatoryDetails.cleanUp(userAnswers, newPartnersIndex1)
      }
      "llp" in {
        val businessData = cleanedDataNewPartners(businessType = 5)
        val userAnswers = UserAnswers(id = mgdRegNumber, modified = ModifiedAnswers(businessData))

        PartnerMandatoryDetails.cleanUp(userAnswers, newPartnersIndex1)
      }
    }

    "required - new partner only" - {
      val idx: BusinessNumberOrIndex = newPartnersIndex1

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
          Seq("partnerDetailsBusinessType",
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
          Seq("partnerDetailsBusinessType",
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
