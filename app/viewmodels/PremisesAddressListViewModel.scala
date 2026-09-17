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

package viewmodels

import models.licencespremises.PremisesDetails
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Key, SummaryList}
import viewmodels.govuk.all.{FluentKey, FluentValue, KeyViewModel, SummaryListRowViewModel, SummaryListViewModel, ValueViewModel}
import viewmodels.implicits.*

case object PremisesAddressListViewModel {
  def from(premisesList: Seq[PremisesDetails])(implicit messages: Messages): SummaryList = {
    // we map each address line to a summary row, which is then fed into the summary list vm
    SummaryListViewModel(
      premisesList.map(addr =>

        val address = Seq(addr.address1, addr.address2, addr.address3, addr.address4, addr.postcode).flatten.mkString(", ")

        SummaryListRowViewModel(
          key   = KeyViewModel("").withCssClass("govuk-!-display-none"),
          value = ValueViewModel(address).withCssClass("govuk-!-width-one-half"),
          actions = Seq(
            ActionItem(
              href               = "#",
              content            = "site.change",
              visuallyHiddenText = Some(Seq(addr.address1, addr.postcode).flatten.mkString(", "))
            ),
            ActionItem(
              href               = "#",
              content            = "site.remove",
              visuallyHiddenText = Some(Seq(addr.address1, addr.postcode).flatten.mkString(", "))
            )
          )
        )
      )
    )
  }
}
