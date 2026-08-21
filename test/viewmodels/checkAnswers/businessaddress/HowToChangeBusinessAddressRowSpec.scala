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
import controllers.routes
import models.BusinessChangeAddrOption.*
import models.{Address, UserAnswers}
import pages.*
import pages.businessaddress.{BusinessAddressUkPage, BusinessChangeAddrScreenerPage}
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.all.{KeyViewModel, SummaryListRowViewModel, ValueViewModel}

class HowToChangeBusinessAddressRowSpec extends SpecBase {

  trait Setup {
    val app: Application = applicationBuilder().build()
    implicit val request: play.api.mvc.Request[?] = FakeRequest()
    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

    val emptySummaryList: SummaryListRow = SummaryListRowViewModel(
      KeyViewModel(Text("")),
      ValueViewModel(Text(""))
    )
  }

  "HowToChangeBusinessAddressRow" - {

    "must show correct data for 'different uk address' option" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))

      private val ua = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessChangeAddrScreenerPage, DifferentUkAddress)
        .success
        .value

      private val result: SummaryListRow = HowToChangeBusinessAddressRow.from(ua).getOrElse(emptySummaryList)
      private val route: String = routes.BusinessChangeAddrScreenerController.onPageLoad().url

      result.key.content mustEqual Text(messages("checkBusinessAddress.question.howToChange"))
      result.value.content mustEqual Text(messages("checkBusinessAddress.changeOption.difUk"))
      result.actions.get.items.head.href mustEqual route

    }

    "must show correct data for 'change to non uk address' option" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))

      private val ua = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessChangeAddrScreenerPage, ChangeToNonUkAddress)
        .success
        .value

      private val emptySummaryList = SummaryListRowViewModel(
        KeyViewModel(Text("")),
        ValueViewModel(Text(""))
      )
      private val result: SummaryListRow = HowToChangeBusinessAddressRow.from(ua).getOrElse(emptySummaryList)

      result.key.content mustEqual Text(messages("checkBusinessAddress.question.howToChange"))
      result.value.content mustEqual Text(messages("checkBusinessAddress.changeOption.nonUk"))
    }

    "must show correct data for 'change to uk address' option" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))

      private val ua: UserAnswers = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessChangeAddrScreenerPage, ChangeToUkAddress)
        .success
        .value

      private val result: SummaryListRow = HowToChangeBusinessAddressRow.from(ua).getOrElse(emptySummaryList)

      result.key.content mustEqual Text(messages("checkBusinessAddress.question.howToChange"))
      result.value.content mustEqual Text(messages("checkBusinessAddress.changeOption.toUk"))
    }

    "must show correct data for 'edit current address' option" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))

      private val ua: UserAnswers = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessChangeAddrScreenerPage, EditCurrentAddress)
        .success
        .value

      private val result: SummaryListRow = HowToChangeBusinessAddressRow.from(ua).getOrElse(emptySummaryList)

      result.key.content mustEqual Text(messages("checkBusinessAddress.question.howToChange"))
      result.value.content mustEqual Text(messages("checkBusinessAddress.changeOption.edit"))
    }
  }
}
