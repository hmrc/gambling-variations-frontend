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
import org.scalatest.matchers.must.Matchers
import viewmodels.govuk.PaginationFluency.*

class PaginationServiceSpec extends SpecBase with Matchers {

  val paginationService = new PaginationService(10, 100, 5)

  "PaginationService" - {

    "paginateDirectDebits" - {

      "must return correct pagination for first page with 10 records per page" in {
        val testData = createTestPartnerDetails(5)
        val result = paginationService.paginatePartnerDetails(testData, currentPage = 1, elementsPerPage = 10, baseUrl = "/test")

        result.paginatedData.length mustBe 5
        result.currentPage mustBe 1
        result.totalPages mustBe 1
        result.totalRecords mustBe 5
        result.paginationViewModel.previous mustBe None
        result.paginationViewModel.next mustBe None
      }

      "must return correct pagination for last page" in {
        val testData = createTestPartnerDetails(25)
        val result = paginationService.paginatePartnerDetails(testData, currentPage = 3, elementsPerPage = 10, baseUrl = "/test")

        result.paginatedData.length mustBe 5
        result.currentPage mustBe 3
        result.totalPages mustBe 3
        result.totalRecords mustBe 25
        result.paginationViewModel.previous mustBe defined
        result.paginationViewModel.next mustBe None
      }

      "must return correct pagination for middle page" in {
        val testData = createTestPartnerDetails(25)
        val result = paginationService.paginatePartnerDetails(testData, currentPage = 2, elementsPerPage = 10, baseUrl = "/test")

        result.paginatedData.length mustBe 10
        result.currentPage mustBe 2
        result.totalPages mustBe 3
        result.totalRecords mustBe 25
        result.paginationViewModel.previous mustBe defined
        result.paginationViewModel.next mustBe defined
      }

      "must handle empty data" in {
        val result = paginationService.paginatePartnerDetails(Seq.empty, currentPage = 1, elementsPerPage = 10, baseUrl = "/test")

        result.paginatedData.length mustBe 0
        result.currentPage mustBe 1
        result.totalPages mustBe 0
        result.totalRecords mustBe 0
        result.paginationViewModel.items.length mustBe 0
      }

      "must limit records to maximum of 99" in {
        val testData = createTestPartnerDetails(150)
        val result = paginationService.paginatePartnerDetails(testData, currentPage = 1, elementsPerPage = 10, baseUrl = "/test")

        result.totalRecords mustBe 100
        result.totalPages mustBe 10
      }

      "must sort by business number" in {
        val testData = Seq(
          "123456789",
          "12345678",
          "123456",
          "12345",
          "1234567"
        )

        val result = paginationService.paginatePartnerDetails(testData, currentPage = 1, elementsPerPage = 10, baseUrl = "/test")

        result.paginatedData.head mustBe "12345"
        result.paginatedData(1) mustBe "123456"
        result.paginatedData(2) mustBe "1234567"
        result.paginatedData(3) mustBe "12345678"
        result.paginatedData(4) mustBe "123456789"
      }

      "must handle invalid page numbers gracefully" in {
        val testData = createTestPartnerDetails(5)

        val resultNegative = paginationService.paginatePartnerDetails(testData, currentPage = -1, elementsPerPage = 10, baseUrl = "/test")
        resultNegative.currentPage mustBe 1

        val resultTooHigh = paginationService.paginatePartnerDetails(testData, currentPage = 999, elementsPerPage = 10, baseUrl = "/test")
        resultTooHigh.currentPage mustBe 1
      }

      "must generate correct pagination links" in {
        val testData = createTestPartnerDetails(25)
        val result = paginationService.paginatePartnerDetails(testData, currentPage = 2, elementsPerPage = 10, baseUrl = "/test")

        result.paginationViewModel.previous.get.href mustBe "/test?page=1"
        result.paginationViewModel.next.get.href mustBe "/test?page=3"
        result.paginationViewModel.items.exists(_.current) mustBe true
        result.paginationViewModel.items.find(_.current).get.number mustBe "2"
      }

      "must not show pagination when only one page" in {
        val testData = createTestPartnerDetails(2)
        val result = paginationService.paginatePartnerDetails(testData, currentPage = 1, elementsPerPage = 10, baseUrl = "/test")

        result.paginationViewModel.items.length mustBe 0
        result.paginationViewModel.previous mustBe None
        result.paginationViewModel.next mustBe None
      }
    }
  }

  private def createTestPartnerDetails(count: Int): Seq[String] = {
    (1 to count).map { i =>
      i.toString
    }
  }
}
