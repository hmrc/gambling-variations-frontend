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
import models.Address
import pages.*
import pages.businessaddress.*
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.all.{KeyViewModel, SummaryListRowViewModel, ValueViewModel}

class HasUkPostcodeRowSpec extends SpecBase {

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

  "HasUkPostcodeRow" - {

    "must render summary list row with correct data" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))

      private val ua = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessUKAddrScreenerPage, true)
        .success
        .value

      private val result: SummaryListRow = HasUkPostcodeRow.from(ua).getOrElse(emptySummaryList)
      private val route: String = routes.BusinessUKAddrScreenerController.onPageLoad().url

      result.key.content mustEqual Text(messages("checkBusinessAddress.question.ukPostcode"))
      result.value.content mustEqual Text(messages("site.yes"))
      result.actions.get.items.head.href mustEqual route
    }
  }
}
