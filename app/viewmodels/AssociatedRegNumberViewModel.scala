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
import models.NormalMode
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.govuk.all.stringToText

case class AssociatedRegNumberViewModel(associatedRegNumbers: Option[Seq[String]]) {

  def summaryList(implicit messages: Messages): Seq[SummaryListRow] = associatedRegNumberSummaryListRows

  private def associatedRegNumberSummaryListRows(implicit messages: Messages): Seq[SummaryListRow] = {
    associatedRegNumbers match {
      case Some(newAssocRegNumbers) =>
        newAssocRegNumbers.map(newAssocReg =>
          SummaryListRow(
            key = Key(content = newAssocReg, classes = s"associated-reg-number associated-reg-number-$newAssocReg govuk-!-font-weight-regular"),
            actions = Some(
              Actions(
                items = Seq(
                  ActionItem(
                    href               = routes.AssociatedRegNumberController.onPageLoad(NormalMode).url,
                    content            = "site.change",
                    visuallyHiddenText = Some(messages("associatedRegistrationNumbers.change.hidden", newAssocReg))
                  ),
                  ActionItem(
                    href               = routes.AssociatedRegistrationNumbersController.onRedirect(assocRegNumber = newAssocReg).url,
                    content            = "site.remove",
                    visuallyHiddenText = Some(messages("associatedRegistrationNumbers.change.hidden", newAssocReg))
                  )
                ),
                classes = "govuk-summary-list__actions govuk-!-width-one-half"
              )
            )
          )
        )
      case None => Seq.empty
    }
  }
}
