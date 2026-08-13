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
import controllers.routes
import play.api.i18n.Messages
import pages.{BusinessAddressNonUkPage, BusinessAddressUkPage}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryListRow, Value}
import viewmodels.govuk.all.{ActionItemViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*

case class BusinessAddressRow(ua: UserAnswers, mode: Mode)(implicit messages: Messages) {
  private val isUk = ua.get(BusinessAddressUkPage).fold(false)(_ => true)
  private val isNonUk = ua.get(BusinessAddressNonUkPage).fold(false)(_ => true)
  private val addressExists = isUk || isNonUk

  def toRow: SummaryListRow = {

    val addressUa: Option[Address] = if (isUk) {
      ua.get(BusinessAddressUkPage)
    } else if (isNonUk) {
      ua.get(BusinessAddressNonUkPage)
    } else {
      None
    }

    val addressRowValue: Value = {
      addressUa match {
        case Some(addr) =>
          Value(
            HtmlContent(
              Html(
                Seq(
                  Some(addr.address1),
                  addr.address2,
                  addr.address3,
                  addr.address4,
                  if (isUk) Some(ua.get(BusinessAddressUkPage).flatMap(addr => addr.postcode).getOrElse("site.notProvided")) else None,
                  if (isNonUk) Some(ua.get(BusinessAddressNonUkPage).flatMap(addr => addr.country).getOrElse("site.notProvided")) else None
                ).flatten.mkString("<br>")
              )
            )
          )
        case None => ValueViewModel("site.notProvided")
      }
    }

    SummaryListRowViewModel(
      key   = "checkBusinessAddress.label.address",
      value = addressRowValue,
      actions = Seq(
        ActionItemViewModel("site.change",
                            if (isUk) {
                              "#"
                            } else if (isNonUk) {
                              "#"
                            } else {
                              routes.BusinessChangeAddrScreenerController.onPageLoad().url
                            }
                           )
      ) ++
        Seq(if (addressExists) {
          Some(ActionItemViewModel("site.remove", "#"))
        } else {
          None
        }).flatten
    )
  }
}
