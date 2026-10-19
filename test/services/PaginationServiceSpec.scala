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

package services

import base.SpecBase
import models.{BusinessType, UserAnswers}
import org.scalatest.matchers.must.Matchers
import pages.partnerdetails.{PartnerDetailsBusinessNamePage, PartnerDetailsBusinessTypePage}
import viewmodels.govuk.PaginationFluency.*

class PaginationServiceSpec extends SpecBase with Matchers {

  val paginationService = new PaginationService(10, 100, 5)

  "PaginationService" - {

    "paginateDirectDebits" - {

      "must return correct pagination for first page with 10 records per page" in {
        val testData = createTestExistingPartnerDetails(5)
        val userAnswers = createValidUserAnswers(5)
        val result =
          paginationService.paginateAlphabeticallyPartnerDetails(
            Seq.empty,
            testData,
            userAnswers = userAnswers,
            currentPage = 1,
            baseUrl     = "/test"
          )

        result.paginatedData.length mustBe 5
        result.currentPage mustBe 1
        result.totalPages mustBe 1
        result.totalRecords mustBe 5
        result.paginationViewModel.previous mustBe None
        result.paginationViewModel.next mustBe None
      }

      "must return correct pagination for last page" in {
        val testData = createTestExistingPartnerDetails(25)
        val userAnswers = createValidUserAnswers(25)
        val result =
          paginationService.paginateAlphabeticallyPartnerDetails(
            Seq.empty,
            testData,
            userAnswers = userAnswers,
            currentPage = 3,
            baseUrl     = "/test"
          )

        result.paginatedData.length mustBe 5
        result.currentPage mustBe 3
        result.totalPages mustBe 3
        result.totalRecords mustBe 25
        result.paginationViewModel.previous mustBe defined
        result.paginationViewModel.next mustBe None
      }

      "must return correct pagination for middle page" in {
        val testData = createTestExistingPartnerDetails(25)
        val userAnswers = createValidUserAnswers(25)
        val result =
          paginationService.paginateAlphabeticallyPartnerDetails(
            Seq.empty,
            testData,
            userAnswers = userAnswers,
            currentPage = 2,
            baseUrl     = "/test"
          )

        result.paginatedData.length mustBe 10
        result.currentPage mustBe 2
        result.totalPages mustBe 3
        result.totalRecords mustBe 25
        result.paginationViewModel.previous mustBe defined
        result.paginationViewModel.next mustBe defined
      }

      "must correctly sort partners and existing partners alphabetically based on the business name" in {
        val testDataExisting = Seq(
          ("a", "business1"),
          ("b", "business7"),
          ("c", "business3"),
          ("d", "business6")
        )
        val testDataNew = Seq(
          (0, "business5"),
          (1, "business2"),
          (2, "business4"),
          (3, "business8")
        )

        val userAnswers = createValidUserAnswers(testDataExisting, testDataNew)
        val result = paginationService.paginateAlphabeticallyPartnerDetails(
          testDataNew.map(_._1),
          testDataExisting.map(_._1),
          userAnswers = userAnswers,
          currentPage = 1,
          baseUrl     = "/test"
        )

        result.paginatedData.head mustBe "a"
        result.paginatedData(1) mustBe 1
        result.paginatedData(2) mustBe "c"
        result.paginatedData(3) mustBe 2
        result.paginatedData(4) mustBe 0
        result.paginatedData(5) mustBe "d"
        result.paginatedData(6) mustBe "b"
        result.paginatedData(7) mustBe 3

        result.paginatedData.length mustBe 8
        result.currentPage mustBe 1
        result.totalPages mustBe 1
        result.totalRecords mustBe 8
      }

      "must handle empty data" in {
        val userAnswers = createValidUserAnswers(0)
        val result =
          paginationService.paginateAlphabeticallyPartnerDetails(
            Seq.empty,
            Seq.empty,
            userAnswers = userAnswers,
            currentPage = 1,
            baseUrl     = "/test"
          )

        result.paginatedData.length mustBe 0
        result.currentPage mustBe 1
        result.totalPages mustBe 0
        result.totalRecords mustBe 0
        result.paginationViewModel.items.length mustBe 0
      }

      "must limit records to maximum of 99" in {
        val testData = createTestExistingPartnerDetails(150)
        val userAnswers = createValidUserAnswers(150)
        val result =
          paginationService.paginateAlphabeticallyPartnerDetails(
            Seq.empty,
            testData,
            userAnswers = userAnswers,
            currentPage = 1,
            baseUrl     = "/test"
          )

        result.totalRecords mustBe 100
        result.totalPages mustBe 10
      }

      "must sort by business name alphabetically" in {
        val testData = Seq(
          ("123456789", "business1"),
          ("12345678", "business3"),
          ("123456", "business4"),
          ("12345", "business2"),
          ("1234567", "business5")
        )
        val userAnswers = createValidUserAnswers(testData, Seq.empty)

        val result =
          paginationService.paginateAlphabeticallyPartnerDetails(
            Seq.empty,
            testData.map(_._1),
            userAnswers = userAnswers,
            currentPage = 1,
            baseUrl     = "/test"
          )

        result.paginatedData.head mustBe "123456789"
        result.paginatedData(1) mustBe "12345"
        result.paginatedData(2) mustBe "12345678"
        result.paginatedData(3) mustBe "123456"
        result.paginatedData(4) mustBe "1234567"
      }

      "must handle invalid page numbers gracefully" in {
        val testData = createTestExistingPartnerDetails(5)
        val userAnswers = createValidUserAnswers(5)

        val resultNegative =
          paginationService.paginateAlphabeticallyPartnerDetails(
            Seq.empty,
            testData,
            userAnswers = userAnswers,
            currentPage = -1,
            baseUrl     = "/test"
          )
        resultNegative.currentPage mustBe 1

        val resultTooHigh =
          paginationService.paginateAlphabeticallyPartnerDetails(
            Seq.empty,
            testData,
            userAnswers = userAnswers,
            currentPage = 999,
            baseUrl     = "/test"
          )
        resultTooHigh.currentPage mustBe 1
      }

      "must generate correct pagination links" in {
        val testData = createTestExistingPartnerDetails(25)
        val userAnswers = createValidUserAnswers(25)
        val result =
          paginationService.paginateAlphabeticallyPartnerDetails(
            Seq.empty,
            testData,
            userAnswers = userAnswers,
            currentPage = 2,
            baseUrl     = "/test"
          )

        result.paginationViewModel.previous.get.href mustBe "/test?page=1"
        result.paginationViewModel.next.get.href mustBe "/test?page=3"
        result.paginationViewModel.items.exists(_.current) mustBe true
        result.paginationViewModel.items.find(_.current).get.number mustBe "2"
      }

      "must not show pagination when only one page" in {
        val testData = createTestExistingPartnerDetails(2)
        val userAnswers = createValidUserAnswers(2)
        val result =
          paginationService.paginateAlphabeticallyPartnerDetails(
            Seq.empty,
            testData,
            userAnswers = userAnswers,
            currentPage = 1,
            baseUrl     = "/test"
          )

        result.paginationViewModel.items.length mustBe 0
        result.paginationViewModel.previous mustBe None
        result.paginationViewModel.next mustBe None
      }
    }
  }

  private def createValidUserAnswers(count: Int): UserAnswers = {
    val userAnswers = UserAnswers("id")

    val userAnswersExistingUsers = (0 to count)
      .foldLeft(userAnswers)((answers, b) =>
        answers
          .set(PartnerDetailsBusinessNamePage(b.toString), b.toString)
          .success
          .value
          .set(PartnerDetailsBusinessTypePage(b.toString), BusinessType.Partnership)
          .success
          .value
      )
    (0 to count)
      .foldLeft(userAnswersExistingUsers)((answers, b) =>
        answers
          .set(PartnerDetailsBusinessNamePage(b), b.toString)
          .success
          .value
          .set(PartnerDetailsBusinessTypePage(b), BusinessType.Partnership)
          .success
          .value
      )
  }

  private def createValidUserAnswers(businessNumbers: Seq[(String, String)], newPartnersIndexes: Seq[(Int, String)]): UserAnswers = {
    val userAnswers = UserAnswers("id")
    val userAnswersExistingUsers = businessNumbers
      .foldLeft(userAnswers)((answers, indexAndBusinessName) =>
        answers
          .set(PartnerDetailsBusinessNamePage(indexAndBusinessName._1), indexAndBusinessName._2)
          .success
          .value
          .set(PartnerDetailsBusinessTypePage(indexAndBusinessName._1), BusinessType.Partnership)
          .success
          .value
      )
    newPartnersIndexes
      .foldLeft(userAnswersExistingUsers)((answers, indexAndBusinessName) =>
        answers
          .set(PartnerDetailsBusinessNamePage(indexAndBusinessName._1), indexAndBusinessName._2)
          .success
          .value
          .set(PartnerDetailsBusinessTypePage(indexAndBusinessName._1), BusinessType.Partnership)
          .success
          .value
      )
  }

  private def createTestExistingPartnerDetails(count: Int): Seq[String] = {
    (1 to count).map { i =>
      i.toString
    }
  }
}
