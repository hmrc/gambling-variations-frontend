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

import controllers.routes
import models.{Address, Mode, UserAnswers}
import play.api.i18n.Messages
import play.api.mvc.Result
import navigation.Navigator
import pages.{BusinessAddressNonUkPage, BusinessAddressSectionPage, BusinessAddressUkPage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryListRow, Value}
import viewmodels.govuk.all.{ActionItemViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*

import java.net.http.HttpClient.Redirect

case class BusinessAddressRow(ua: UserAnswers, isUk: Boolean, isNonUk: Boolean, mode: Mode)
                             (implicit messages: Messages, navigator: Navigator) {
  def toRow: SummaryListRow = {

    val addressUa: Option[Address] = if (isUk) {
      ua.get(BusinessAddressUkPage)
    } else if(isNonUk){
      ua.get(BusinessAddressNonUkPage)
    } else {
      None
    }

    val addressRowValue: Value = {
      addressUa match {
        case Some(addr) =>
          ValueViewModel(
            s"""
                         ${addr.address1}
                         ${addr.address2.getOrElse(messages("site.notProvided"))}
                         ${addr.address2.getOrElse(messages("site.notProvided"))}
                         ${addr.address3.getOrElse(messages("site.notProvided"))}
                         ${addr.address4.getOrElse(messages("site.notProvided"))}
                         ${if (isUk) addr.postcode.getOrElse("site.notProvided")}
                         ${if (!isUk) addr.country.getOrElse("site.notProvided")}
                         """
          )
        case None => ValueViewModel("site.notProvided")
      }

    }

    val changeRoute: String =
      if(isUk) {
        "#"
      } else if(isNonUk) {
        "#"
      } else {
        "#"
      }



    SummaryListRowViewModel(
      key     = "checkBusinessAddress.label.address",
      value   = addressRowValue,
      actions = Seq(
        ActionItemViewModel("site.change", "#"),
        ActionItemViewModel("site.remove", "#"),
      )
    )
  }
}
