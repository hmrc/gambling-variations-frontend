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

package controllers.partnerdetails

import models.UserAnswers
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import services.{PaginationResult, PaginationService}
import viewmodels.govuk.PaginationFluency.PaginationViewModel

import java.time.LocalDate

trait PartnerDetailsHelper {

  val businessNumber1: String = "12345"
  val businessNumber2: String = "123456"
  val businessNumber3: String = "1234567"
  val newPartnersIndex1: Int = 0
  val newPartnersIndex2: Int = 1

  val mgdRegNumber: String = "XGM00000001761"
  val testFaxNumber: String = "0123456789"
  val testPhoneNumber: String = "0123456789"
  val testEmailAddress: String = "test@test.com"
  val testNino: String = "SR123456A"
  val testVRN: String = "353868127"
  val testUtr: String = "1121766916"
  val testForeignCorpRef = "FCR-987654"

  lazy val onwardRoute: Call = Call("GET", "/foo")

  val todayDate = LocalDate.of(2026, 1, 1)

  class PaginationHelper(size: 1 | 2 | 3 | 10 | 100, currentPage: Int = 1) {
    val partnerDetailsBusinessNumberList = size match {
      case 1   => Seq(businessNumber1)
      case 2   => Seq(businessNumber1, businessNumber2)
      case 3   => Seq(businessNumber1, businessNumber2, businessNumber3)
      case 10  => (1 to 10).map(_.toString) // Ten numbers in the same pattern
      case 100 => (1 to 100).map(_.toString) // 100 numbers in the same pattern
    }

    val paginationService = new PaginationService(10, 100, 5)
    val paginationResult: PaginationResult = paginationService.paginatePartnerDetails(
      existingPartnerDetails = partnerDetailsBusinessNumberList,
      currentPage            = currentPage,
      elementsPerPage        = 10,
      // Note: I had to prepend "gambling-variations" when testing in IDE, in sbt works fine
      baseUrl = routes.PartnerDetailsController.onPageLoad(None).url
    )
    val elementsPerPage = 10
    val paginatedViewModel: PaginationViewModel = paginationResult.paginationViewModel
    val page: Int = currentPage
    val totalRecords: Int = size

    val from: Int = page * elementsPerPage - elementsPerPage + 1
    val to: Int = (page * elementsPerPage).min(totalRecords)
  }

  def emptyData = Json.obj()

  def minimalValidData = Json.obj(
    "partners" -> Json.obj()
  )

  def userAnswersPartnerDetailsEmpty: UserAnswers = UserAnswers("id")
  def userAnswersPartnerDetailsMinimalValidData: UserAnswers = UserAnswers("id", minimalValidData)
  def userAnswersPartnerDetailsExistingPartners: UserAnswers = UserAnswers("id", cleanedDataExistingPartners())
  def userAnswersPartnerDetailsNewPartners: UserAnswers = UserAnswers("id", cleanedDataNewPartners())

  def cleanedDataExistingPartners(
    faxNumber: Option[String] = None,
    phoneNumber: Option[String] = None,
    mobilePhoneNumber: Option[String] = None,
    emailAddress: Option[String] = None,
    additionalInformation: Option[String] = Some("ADI123456"),
    nino: Option[String] = None,
    vrn: Option[String] = None,
    utr: Option[String] = None,
    fcr: Option[String] = None
  ): JsObject = Json.obj(
    "partners" -> Json.obj(
      businessNumber1 -> Json.obj(
        "partnerDetailsMgdRegNumber"  -> mgdRegNumber,
        "partnerDetailsDateOfJoining" -> "2022-01-15",
        "partnerDetailsDateOfLeaving" -> "2028-12-31",
        "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
          "mgdRegNumber" -> mgdRegNumber,
          "correspondenceAddress" -> Json.obj(
            "address1" -> "123 High Street",
            "address2" -> "Suite 4",
            "address3" -> "Business Park",
            "address4" -> "London",
            "postcode" -> "SW1A 1AA",
            "country"  -> "GB"
          ),
          "additionalInformation" -> additionalInformation,
          "iomOrCiFlag"           -> "N",
          "contactNumber" -> Json.obj(
            "phoneNumber"       -> phoneNumber,
            "mobilePhoneNumber" -> mobilePhoneNumber
          ),
          "faxNumber" -> faxNumber,
          "emailAddr" -> emailAddress
        ),
        "partnerDetailsDateOfIncorporation"    -> "2020-03-01",
        "partnerDetailsCountryOfIncorporation" -> None,
        "partnerDetailsBusinessName"           -> "XYZ Consulting Ltd",
        "partnerDetailsTradingName"            -> "XYZ Consulting",
        "partnerDetailsDateOfBirth"            -> "1985-06-20",
        "partnerDetailsNino"                   -> nino,
        "partnerDetailsUtr"                    -> utr,
        "partnerDetailsVrn"                    -> vrn,
        "partnerDetailsCrn"                    -> "09876543",
        "partnerDetailsForeignCorporateRef"    -> fcr,
        "partnerDetailsIsFutureLeaveDate"      -> 0,
        "partnerDetailsIsFutureJoinDate"       -> 0,
        "partnerDetailsBusinessType"           -> 1
      )
    )
  )

  def cleanedDataNewPartners(
    faxNumber: Option[String] = None,
    phoneNumber: Option[String] = None,
    mobilePhoneNumber: Option[String] = None,
    emailAddress: Option[String] = None,
    additionalInformation: Option[String] = Some("ADI123456"),
    nino: Option[String] = None,
    vrn: Option[String] = None,
    utr: Option[String] = None,
    fcr: Option[String] = None
  ): JsObject = Json.obj(
    "partners" -> Json.obj(),
    "newPartners" -> Json.arr(
      Json.obj(
        "partnerDetailsMgdRegNumber"  -> mgdRegNumber,
        "partnerDetailsDateOfJoining" -> "2022-01-15",
        "partnerDetailsDateOfLeaving" -> "2028-12-31",
        "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
          "mgdRegNumber" -> mgdRegNumber,
          "correspondenceAddress" -> Json.obj(
            "address1" -> "123 High Street",
            "address2" -> "Suite 4",
            "address3" -> "Business Park",
            "address4" -> "London",
            "postcode" -> "SW1A 1AA",
            "country"  -> "GB"
          ),
          "additionalInformation" -> additionalInformation,
          "iomOrCiFlag"           -> "N",
          "contactNumber" -> Json.obj(
            "phoneNumber"       -> phoneNumber,
            "mobilePhoneNumber" -> mobilePhoneNumber
          ),
          "faxNumber" -> faxNumber,
          "emailAddr" -> emailAddress
        ),
        "partnerDetailsDateOfIncorporation"    -> "2020-03-01",
        "partnerDetailsCountryOfIncorporation" -> null,
        "partnerDetailsBusinessName"           -> "XYZ Consulting Ltd",
        "partnerDetailsTradingName"            -> "XYZ Consulting",
        "partnerDetailsDateOfBirth"            -> "1985-06-20",
        "partnerDetailsNino"                   -> nino,
        "partnerDetailsUtr"                    -> utr,
        "partnerDetailsVrn"                    -> vrn,
        "partnerDetailsCrn"                    -> "09876543",
        "partnerDetailsForeignCorporateRef"    -> fcr,
        "partnerDetailsIsFutureLeaveDate"      -> 0,
        "partnerDetailsIsFutureJoinDate"       -> 0,
        "partnerDetailsBusinessType"           -> 1
      )
    )
  )
}
