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
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.govuk.all.{FluentValue, stringToText}

case class CheckBusinessContactDetailsViewModel(phoneNumber: Option[String],
                                                mobilePhoneNumber: Option[String],
                                                faxNumber: Option[String],
                                                emailAddress: Option[String]
                                               ) {

  def summaryList(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    contactNumbersSummaryListRow,
    faxNumbersSummaryListRow,
    emailAddressSummarListRow
  )

  private def contactNumbersSummaryListRow(implicit messages: Messages): SummaryListRow = {
    SummaryListRow(
      key = Key(
        content = messages("contactDetails.heading.contactNumbers")
      ),
      value = Value(content = contactNumbersContent()).withCssClass("contact-numbers"),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href               = controllers.routes.BusinessContactNumberController.onPageLoad(NormalMode).url,
              content            = "site.change",
              visuallyHiddenText = Some(messages("contactDetails.label.contactNumbers"))
            )
          ),
          classes = "govuk-summary-list__actions govuk-!-width-one-third"
        )
      )
    )
  }

  private def faxNumbersSummaryListRow(implicit messages: Messages): SummaryListRow = {
    SummaryListRow(
      key = Key(
        content = messages("contactDetails.heading.faxNumber")
      ),
      value = Value(
        content = faxNumberContent
      ).withCssClass("fax-number"),
      actions = if (faxNumber.isEmpty) {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.routes.FaxNumberController.onPageLoad(NormalMode).url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("contactDetails.label.faxNumber"))
              )
            )
          )
        )
      } else {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.routes.FaxNumberController.onPageLoad(NormalMode).url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("contactDetails.label.faxNumber"))
              ),
              ActionItem(
                href               = controllers.routes.RemoveFaxNumberController.onPageLoad(NormalMode).url,
                content            = "site.remove",
                visuallyHiddenText = Some(messages("contactDetails.label.faxNumber"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
    )
  }

  private def emailAddressSummarListRow(implicit messages: Messages): SummaryListRow = {
    SummaryListRow(
      key = Key(
        content = messages("contactDetails.heading.emailAddr")
      ),
      value = Value(
        content = emailAddress getOrElse messages("contactDetails.message.notProvided")
      ).withCssClass("email-address"),
      actions = if (emailAddress.isEmpty) {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.routes.BusinessEmailAddressController.onPageLoad(NormalMode).url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("contactDetails.label.emailAddr"))
              )
            )
          )
        )
      } else {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.routes.BusinessEmailAddressController.onPageLoad(NormalMode).url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("contactDetails.label.emailAddr"))
              ),
              ActionItem(
                href               = controllers.routes.RemoveEmailAddressController.onPageLoad(NormalMode).url,
                content            = "site.remove",
                visuallyHiddenText = Some(messages("contactDetails.label.emailAddr"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
    )
  }

  private def contactNumbersContent(implicit messages: Messages): Content = {
    if (phoneNumber.isEmpty && mobilePhoneNumber.isEmpty) {
      messages("contactDetails.message.notProvided")
    } else {
      HtmlContent(
        Html(
          s"${messages("contactDetails.label.phoneNumber")}" +
            "<br>" +
            s"${phoneNumber getOrElse messages("contactDetails.message.notProvided")}" +
            "<br>" +
            "<br>" +
            s"${messages("contactDetails.label.mobilePhoneNumber")}" +
            "<br>" +
            s"${mobilePhoneNumber getOrElse messages("contactDetails.message.notProvided")}"
        )
      )
    }
  }

  private def faxNumberContent(implicit messages: Messages): Content = {
    faxNumber match {
      case None => messages("contactDetails.message.notProvided")
      case Some(content) =>
        HtmlContent(
          s"""<span style="white-space: pre-wrap">$content</span>"""
        )
    }
  }

}
