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

import controllers.routes
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

case class AssociatedRegNumbersViewModel(associatedRegNumbers: Option[Seq[String]]) {

  def summaryList(implicit messages: Messages): Seq[SummaryListRow] = {
    associatedRegNumbers match {
      case Some(newAssocRegNumbers) =>
        newAssocRegNumbers.map(newAssocReg =>
          SummaryListRow(
            key = Key(content = Text(newAssocReg), classes = s"associated-reg-number associated-reg-number-$newAssocReg govuk-!-font-weight-regular"),
            actions = Some(
              Actions(
                classes = "govuk-summary-list__actions govuk-!-width-one-half",
                items = Seq(
                  ActionItem(
                    href               = "#",
                    content            = Text(messages("site.change")),
                    visuallyHiddenText = Some(messages("associatedRegistrationNumbers.change.hidden", newAssocReg))
                  ),
                  ActionItem(
                    href               = routes.AssociatedRegistrationNumbersController.onRedirect(assocRegNumber = newAssocReg).url,
                    content            = Text(messages("site.remove")),
                    visuallyHiddenText = Some(messages("associatedRegistrationNumbers.change.hidden", newAssocReg))
                  )
                )
              )
            )
          )
        )
      case None => Seq.empty
    }
  }
}
