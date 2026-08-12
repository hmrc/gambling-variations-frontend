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

import models.{Address, Mode, UserAnswers}
import play.api.i18n.Messages
import pages.{BusinessAddressNonUkPage, BusinessAddressUkPage}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryListRow, Value}
import viewmodels.govuk.all.{ActionItemViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*

case class BusinessAddressRow(ua: UserAnswers, mode: Mode)(implicit messages: Messages) {
  def toRow: SummaryListRow = {
    val isUk = ua.get(BusinessAddressUkPage).fold(false)(_ => true)
    val isNonUk = ua.get(BusinessAddressNonUkPage).fold(false)(_ => true)

    val addressUa: Option[Address] = if (isUk) {
      ua.get(BusinessAddressUkPage)
    } else if (isNonUk) {
      ua.get(BusinessAddressNonUkPage)
    } else {
      None
    }

    val postcodeRow: String = ua.get(BusinessAddressUkPage).flatMap(addr => addr.postcode).getOrElse("site.notProvided")
    val countryRow: String = ua.get(BusinessAddressNonUkPage).flatMap(addr => addr.country).getOrElse("site.notProvided")

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
                         ${if (isUk) postcodeRow else None}
                         ${if (isNonUk) countryRow else None}
                         """
          )
        case None => ValueViewModel("site.notProvided")
      }
    }

    val changeRoute: String =
      if (isUk) {
        "#"
      } else if (isNonUk) {
        "#"
      } else {
        "#"
      }

    SummaryListRowViewModel(
      key   = "checkBusinessAddress.label.address",
      value = addressRowValue,
      actions = Seq(
        ActionItemViewModel("site.change", "#"),
        ActionItemViewModel("site.remove", "#")
      )
    )
  }
}
