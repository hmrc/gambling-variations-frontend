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
import models.{Address, NormalMode}
import pages.*
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class BusinessAddressRowSpec extends SpecBase {

  trait Setup {
    val app: Application = applicationBuilder().build()
    implicit val request: play.api.mvc.Request[?] = FakeRequest()
    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }

  "BusinessAddressRow" - {

    "must render uk address without country and with postcode" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))

      private val ua = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value

      private val result: SummaryListRow = BusinessAddressRow(ua).toRow

      result.key.content mustEqual Text(messages("checkBusinessAddress.label.address"))
      result.value.content mustEqual HtmlContent("address1<br>address2<br>address3<br>address4<br>postcode")
    }

    "must render non uk address without postcode and with country" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))

      private val ua = emptyUserAnswers
        .set(BusinessAddressNonUkPage, addressUa)
        .success
        .value

      private val result: SummaryListRow = BusinessAddressRow(ua).toRow

      result.key.content mustEqual Text(messages("checkBusinessAddress.label.address"))
      result.value.content mustEqual HtmlContent(s"address1<br>address2<br>address3<br>address4<br>country")
    }
  }
}
