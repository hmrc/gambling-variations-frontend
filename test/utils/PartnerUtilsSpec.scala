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

package utils

import base.SpecBase
import models.{BusinessType, CheckMode, NormalMode, SoleProprietorName, UserAnswers}
import org.scalatest.matchers.should.Matchers.shouldBe
import pages.partnerdetails.*

class PartnerUtilsSpec extends SpecBase {

  private val partnerUtils = PartnerUtils

  "PartnerUtils" - {

    "parseIndex" - {

      "correctly determinate string or int" in {
        val stringIndex = "0"

        partnerUtils.parseIndex(stringIndex, NormalMode) shouldBe a[Int]

        partnerUtils.parseIndex(stringIndex, CheckMode) shouldBe a[String]
      }
    }

    "getExistingPartnersBusinessNumbers" - {

      "correctly lists all existing partners in the newPartners array" in {
        val userAnswersExistingPartners = (51 to 100).foldLeft(UserAnswers("id"))((userAnswers, i) =>
          userAnswers.set(PartnerDetailsMgdRegNumberPage(i.toString), i.toString).success.value
        )
        val userAnswers = (0 to 50).foldLeft(userAnswersExistingPartners)((userAnswers, i) =>
          userAnswers.set(PartnerDetailsMgdRegNumberPage(i), i.toString).success.value
        )

        val result = partnerUtils.getExistingPartnersBusinessNumbers(userAnswers)

        result.size mustBe 50
        (0 to 50).forall(e => result(e) == (e + 50).toString)
      }

      "lists empty array when no results are found" in {
        val userAnswers = UserAnswers("id")

        val result = partnerUtils.getExistingPartnersBusinessNumbers(userAnswers)

        result.size mustBe 0
      }

    }

    "findIndexForNewPartner" - {

      "returns first correct index when there is new partner with partnerDetailsAddPartnerCompleted set to false" in {
        val userAnswers = UserAnswers("id")
          .set(PartnerDetailsAddPartnerCompletedPage(0), true)
          .success
          .value
          .set(PartnerDetailsAddPartnerCompletedPage(1), true)
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage(2), "mgdRegNumber")
          .success
          .value
          .set(PartnerDetailsAddPartnerCompletedPage(3), false)
          .success
          .value
          .set(PartnerDetailsAddPartnerCompletedPage(4), false)
          .success
          .value

        val result = partnerUtils.findIndexForNewPartner(userAnswers)

        result mustBe 3
      }

      "returns 0 when all new partners don't have partnerDetailsAddPartnerCompleted" in {
        val userAnswers = UserAnswers("id")
          .set(PartnerDetailsMgdRegNumberPage(0), "mgdRegNumber0")
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage(1), "mgdRegNumber1")
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage(2), "mgdRegNumber2")
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage(3), "mgdRegNumber3")
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage(4), "mgdRegNumber4")
          .success
          .value

        val result = partnerUtils.findIndexForNewPartner(userAnswers)

        result shouldBe 0
      }

      "returns 0 when there is no data" in {
        val userAnswers = UserAnswers("id")

        val result = partnerUtils.findIndexForNewPartner(userAnswers)

        result shouldBe 0
      }

    }

    "getCompletedNewPartners" - {

      "correctly lists indexes of new partners who's journey is complete" in {
        val newPartners: Seq[(Int, Boolean)] = (0 to 50).map(index => (index, if index % 2 == 0 then true else false))

        val userAnswers = newPartners.foldLeft(UserAnswers("id"))((userAnswers, tuple) =>
          userAnswers.set(PartnerDetailsAddPartnerCompletedPage(tuple._1), tuple._2).success.value
        )

        val result = partnerUtils.getCompletedNewPartners(userAnswers)

        result.forall(e => e % 2 == 0) mustBe true
      }

      "omits new partners where partnerDetailsAddPartnerCompleted page is missing" in {
        val newPartners: Seq[(Int, Boolean)] = (0 to 49).map(index => (index, if index % 2 == 0 then true else false))

        val userAnswers = newPartners.foldLeft(UserAnswers("id"))((userAnswers, tuple) =>
          userAnswers.set(PartnerDetailsAddPartnerCompletedPage(tuple._1), tuple._2).success.value
        )

        val userAnswers2 =
          (50 to 59).foldLeft(userAnswers)((userAnswers, index) => userAnswers.set(PartnerDetailsMgdRegNumberPage(index), "mgdNumber").success.value)

        val result = partnerUtils.getCompletedNewPartners(userAnswers2)

        val newPartnersSize = partnerUtils.getNewPartnersSize(userAnswers2)

        newPartnersSize mustBe 60
        result.count(e => e % 2 == 0) mustBe 25
        result.forall(e => e % 2 == 0) mustBe true
      }

      "returns empty list when there are no new partners" in {
        val userAnswers = UserAnswers("id")
        val result = partnerUtils.getCompletedNewPartners(userAnswers)

        result.size mustBe 0
      }

    }

    "getPartnerDetailsName" - {

      "correctly retrieves data in order for sole proprietor" in {
        val userAnswers = UserAnswers("id")
          .set(PartnerDetailsBusinessTypePage(0), BusinessType.Soleproprietor)
          .success
          .value
          .set(PartnerDetailsSoleProprietorPage(0),
               SoleProprietorName(title = "Mr", firstName = "Tom", middleName = Some("John"), lastName = "Smith")
              )
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage(0), "mgd0")
          .success
          .value
          .set(PartnerDetailsBusinessTypePage(1), BusinessType.Soleproprietor)
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage(1), "mgd1")
          .success
          .value
          .set(PartnerDetailsBusinessTypePage(2), BusinessType.Soleproprietor)
          .success
          .value
          .set(PartnerDetailsBusinessNamePage(3), "mgd3")
          .success
          .value

        val result1 = partnerUtils.getPartnerDetailsName(0, userAnswers)
        val result2 = partnerUtils.getPartnerDetailsName(1, userAnswers)
        val result3 = partnerUtils.getPartnerDetailsName(2, userAnswers)
        val result4 = partnerUtils.getPartnerDetailsName(3, userAnswers)

        partnerUtils.getNewPartnersSize(userAnswers) mustBe 4
        result1 mustBe Some("Mr Tom John Smith")
        result2 mustBe Some("mgd1")
        result3 mustBe None
        result4 mustBe None
      }

      "correctly retrieves data in order for business type" in {
        val userAnswers = UserAnswers("id")
          .set(PartnerDetailsBusinessTypePage("0"), BusinessType.Partnership)
          .success
          .value
          .set(PartnerDetailsBusinessNamePage("0"), "business0")
          .success
          .value
          .set(PartnerDetailsTradingNamePage("0"), "tradingName0")
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage("0"), "mgd0")
          .success
          .value
          .set(PartnerDetailsBusinessTypePage("1"), BusinessType.Corporatebody)
          .success
          .value
          .set(PartnerDetailsTradingNamePage("1"), "tradingName1")
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage("1"), "mgd1")
          .success
          .value
          .set(PartnerDetailsBusinessTypePage("2"), BusinessType.Unincorporatedbody)
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage("2"), "mgd2")
          .success
          .value
          .set(PartnerDetailsBusinessTypePage("3"), BusinessType.LimitedLiabilityPartnership)
          .success
          .value
          .set(PartnerDetailsBusinessNamePage("4"), "mgd3")
          .success
          .value

        val result1 = partnerUtils.getPartnerDetailsName("0", userAnswers)
        val result2 = partnerUtils.getPartnerDetailsName("1", userAnswers)
        val result3 = partnerUtils.getPartnerDetailsName("2", userAnswers)
        val result4 = partnerUtils.getPartnerDetailsName("3", userAnswers)
        val result5 = partnerUtils.getPartnerDetailsName("4", userAnswers)

        partnerUtils.getExistingPartnersBusinessNumbers(userAnswers).size mustBe 5
        result1 mustBe Some("business0")
        result2 mustBe Some("tradingName1")
        result3 mustBe Some("mgd2")
        result4 mustBe None
        result5 mustBe None
      }

      "fails to fetch any data if businessType is missing" in {
        val userAnswers = UserAnswers("id")
          .set(PartnerDetailsBusinessNamePage("0"), "business0")
          .success
          .value
          .set(PartnerDetailsTradingNamePage("0"), "tradingName0")
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage("0"), "mgd0")
          .success
          .value

        val result1 = partnerUtils.getPartnerDetailsName("0", userAnswers)
        partnerUtils.getExistingPartnersBusinessNumbers(userAnswers).size mustBe 1

        result1 mustBe None
      }

    }

    "getNewPartnersSize" - {
      "correctly returns a size of new partners" in {
        val userAnswers = (0 to 9).foldLeft(UserAnswers("id"))((userAnswers, index) =>
          userAnswers.set(PartnerDetailsMgdRegNumberPage(index), index.toString).success.value
        )

        val result = partnerUtils.getNewPartnersSize(userAnswers)

        result shouldBe 10
      }

      "correctly returns 0 when there is no data" in {
        val userAnswers = UserAnswers("id")

        val result = partnerUtils.getNewPartnersSize(userAnswers)

        result shouldBe 0
      }

    }

  }

}
