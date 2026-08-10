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
import models.{Address, UserAnswers}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryListRow, Value}
import viewmodels.govuk.all.{ActionItemViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*

case class BusinessAddressRow(address: Option[Address], isUk: Boolean)(implicit messages: Messages) {
  def toRow: SummaryListRow = {

    val addressRowValue: Value = {
      address match {
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

    SummaryListRowViewModel(
      key     = "checkBusinessAddress.label.address",
      value   = addressRowValue,
      actions = Seq(ActionItemViewModel("site.change", routes.PageNotFoundController.onPageLoad().url))
    )
  }
}
