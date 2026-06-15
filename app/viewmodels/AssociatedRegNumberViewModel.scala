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

import models.NormalMode
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.govuk.all.{FluentValue, stringToText}

case class AssociatedRegNumberViewModel(associatedRegNumbers: Option[Seq[String]]) {

  def summaryList(implicit messages: Messages): Seq[SummaryListRow] = associatedRegNumberSummaryListRows


  private def associatedRegNumberSummaryListRows(implicit messages: Messages): Seq[SummaryListRow] = {
    associatedRegNumbers match {
      case Some(associatedRegNums) => for (assocReg <- associatedRegNums) yield {
        SummaryListRow(
          value = Value(
            content = assocReg,
            classes = "associated-registration-number"),
          actions =
            Some(
              Actions(
                items = Seq(
                  ActionItem(
                    href = "#",
                    content = "site.change",
                    visuallyHiddenText = Some(messages("contactDetails.label.faxNumber"))
                  ),
                  ActionItem(
                    href = "#",
                    content = "site.remove",
                    visuallyHiddenText = Some(messages("contactDetails.label.faxNumber"))
                  )
                ),
                //            classes = "govuk-summary-list__actions govuk-!-width-one-half"
              )
            )
        )
      }
      case None =>
        Seq(SummaryListRow(
          value = Value(
            content = messages("contactDetails.message.notProvided")
          ).withCssClass("associated-registration-number"),
          actions =
            Some(
              Actions(
                items = Seq(
                  ActionItem(
                    href = "#",
                    content = "site.change",
                    visuallyHiddenText = Some(messages("contactDetails.label.faxNumber"))
                  ),
                )
              )
            )
        ))
      }
    }
  }
