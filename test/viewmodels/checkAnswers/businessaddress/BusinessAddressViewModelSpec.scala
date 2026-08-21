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

package viewmodels.checkAnswers.businessaddress

import base.SpecBase
import models.BusinessChangeAddrOption.DifferentUkAddress
import models.Address
import pages.*
import pages.businessaddress.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

class BusinessAddressViewModelSpec extends SpecBase {

  trait Setup {
    val app = applicationBuilder().build()
    implicit val request: play.api.mvc.Request[?] = FakeRequest()
    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

  }

  "BusinessAddressViewModel" - {

    "must only show base rows if no screener questions answered" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))
      private val ua = emptyUserAnswers.set(BusinessAddressUkPage, addressUa).success.value

      private val result: SummaryList = BusinessAddressViewModel.from(ua)

      result.rows.size mustEqual 2
      result.rows.head.key.content mustEqual Text(messages("checkBusinessAddress.label.address"))
      result.rows(1).key.content mustEqual Text(messages("checkBusinessAddress.label.addInfo"))
    }

    "must show yesNo rows if add flow & screener questions answered" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))
      private val ua = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessUKAddrScreenerPage, true)
        .success
        .value
        .set(AddBusinessAddressAdditionalInformationPage, true)
        .success
        .value
        .set(BusinessAddressAddFlowPage, true)
        .success
        .value

      private val result: SummaryList = BusinessAddressViewModel.from(ua)

      result.rows.size mustEqual 4
      result.rows.head.key.content mustEqual Text(messages("checkBusinessAddress.question.ukPostcode"))
      result.rows(1).key.content mustEqual Text(messages("checkBusinessAddress.label.address"))
      result.rows(2).key.content mustEqual Text(messages("checkBusinessAddress.question.addInfo"))
      result.rows(3).key.content mustEqual Text(messages("checkBusinessAddress.label.addInfo"))
    }

    "must not show yesNo rows if screener questions answered" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))
      private val ua = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessUKAddrScreenerPage, true)
        .success
        .value
        .set(AddBusinessAddressAdditionalInformationPage, true)
        .success
        .value

      private val result: SummaryList = BusinessAddressViewModel.from(ua)

      result.rows.size mustEqual 2
      result.rows.head.key.content mustEqual Text(messages("checkBusinessAddress.label.address"))
      result.rows(1).key.content mustEqual Text(messages("checkBusinessAddress.label.addInfo"))

    }

    "must show only HasUkPostcodeRow with base in add flow if answered" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))
      private val ua = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessUKAddrScreenerPage, true)
        .success
        .value
        .set(BusinessAddressAddFlowPage, true)
        .success
        .value

      private val result: SummaryList = BusinessAddressViewModel.from(ua)

      result.rows.size mustEqual 3
      result.rows.head.key.content mustEqual Text(messages("checkBusinessAddress.question.ukPostcode"))
      result.rows(1).key.content mustEqual Text(messages("checkBusinessAddress.label.address"))
      result.rows(2).key.content mustEqual Text(messages("checkBusinessAddress.label.addInfo"))

    }

    "must show only AddAddressAdditionalInfoRow with base in add flow if answered" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))
      private val ua = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(AddBusinessAddressAdditionalInformationPage, true)
        .success
        .value
        .set(BusinessAddressAddFlowPage, true)
        .success
        .value

      private val result: SummaryList = BusinessAddressViewModel.from(ua)

      result.rows.size mustEqual 3
      result.rows.head.key.content mustEqual Text(messages("checkBusinessAddress.label.address"))
      result.rows(1).key.content mustEqual Text(messages("checkBusinessAddress.question.addInfo"))
      result.rows(2).key.content mustEqual Text(messages("checkBusinessAddress.label.addInfo"))
    }

    "must show HowToChangeBusinessAddressRow with base if answered" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))
      private val ua = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessChangeAddrScreenerPage, DifferentUkAddress)
        .success
        .value

      private val result: SummaryList = BusinessAddressViewModel.from(ua)

      result.rows.size mustEqual 3
      result.rows.head.key.content mustEqual Text(messages("checkBusinessAddress.question.howToChange"))
      result.rows.head.value.content mustEqual Text(messages("checkBusinessAddress.changeOption.difUk"))
      result.rows(1).key.content mustEqual Text(messages("checkBusinessAddress.label.address"))
      result.rows(2).key.content mustEqual Text(messages("checkBusinessAddress.label.addInfo"))
    }
  }
}
