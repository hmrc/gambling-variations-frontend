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

import models.{Address, NormalMode}
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.govuk.all.{FluentValue, stringToText}

case class CheckCorrespondenceDetailsViewModel(correspondenceName: Option[String],
                                               addCorrespondenceAdditionalName: Option[Boolean],
                                               additionalCorrespondenceName: Option[String],
                                               correspondenceAddress: Option[Address],
                                               addCorrespondenceAdditionalInformation: Option[Boolean],
                                               correspondenceAdditionalInformation: Option[String],
                                               phoneNumber: Option[String],
                                               mobilePhoneNumber: Option[String],
                                               addCorrespondenceFaxNumber: Option[Boolean],
                                               faxNumber: Option[String],
                                               addCorrespondenceEmailAddress: Option[Boolean],
                                               emailAddress: Option[String],
                                               isSubmitted: Boolean
                                              ) {

  def summaryList(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    Some(correspondenceNameSummaryListRow),
    addAdditionalCorrespondenceNameSummaryListRow,
    Some(additionalCorrespondenceNameSummaryListRow),
    Some(correspondenceAddressUkSummaryListRow),
    addAdditionalInformationSummaryListRow,
    Some(additionalInformationSummaryListRow),
    Some(contactNumbersSummaryListRow),
    addFaxNumberSummaryListRow,
    Some(faxNumberSummaryListRow),
    addEmailAddressSummaryListRow,
    Some(emailAddressSummaryListRow)
  ).flatten

  private def correspondenceNameSummaryListRow(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = messages("checkCorrespondenceDetails.heading.correspondenceName")
      ),
      value = Value(
        content = correspondenceName getOrElse messages("checkCorrespondenceDetails.message.notProvided")
      ),
      actions = if (correspondenceName.isEmpty) {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.correspondenceName"))
              )
            )
          )
        )
      } else {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.correspondenceName"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
    )

  private def addAdditionalCorrespondenceNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    addCorrespondenceAdditionalName map { add =>
      SummaryListRow(
        key = Key(
          content = messages("checkCorrespondenceDetails.heading.addAdditionalCorrespondenceName")
        ),
        value = Value(
          content = if (add) {
            messages("site.yes")
          } else {
            messages("site.no")
          }
        ),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.addAdditionalCorrespondenceName"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      )
    }

  private def additionalCorrespondenceNameSummaryListRow(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = messages("checkCorrespondenceDetails.heading.additionalCorrespondenceName")
      ),
      value = Value(
        content = additionalCorrespondenceName getOrElse messages("checkCorrespondenceDetails.message.notProvided")
      ),
      actions = if (additionalCorrespondenceName.isEmpty) {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.additionalCorrespondenceName"))
              )
            )
          )
        )
      } else {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.additionalCorrespondenceName"))
              ),
              ActionItem(
                href               = "#",
                content            = "site.remove",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.additionalCorrespondenceName"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
    )

  private def correspondenceAddressUkSummaryListRow(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = messages("checkCorrespondenceDetails.heading.correspondenceAddress")
      ),
      value = Value(
        content = addressContent
      ),
      actions = if (correspondenceName.isEmpty) {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.correspondenceAddress"))
              )
            )
          )
        )
      } else {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.correspondenceAddress"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
    )

  private def addAdditionalInformationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    addCorrespondenceAdditionalInformation map { add =>
      SummaryListRow(
        key = Key(
          content = messages("checkCorrespondenceDetails.heading.addAdditionalInformation")
        ),
        value = Value(
          content = if (add) {
            messages("site.yes")
          } else {
            messages("site.no")
          }
        ),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.addAdditionalInformation"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      )
    }

  private def additionalInformationSummaryListRow(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = messages("checkCorrespondenceDetails.heading.additionalCorrespondenceInformation")
      ),
      value = Value(
        content = correspondenceAdditionalInformation getOrElse messages("checkCorrespondenceDetails.message.notProvided")
      ),
      actions = if (correspondenceAdditionalInformation.isEmpty) {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.additionalCorrespondenceInformation"))
              )
            )
          )
        )
      } else {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.additionalCorrespondenceInformation"))
              ),
              ActionItem(
                href               = "#",
                content            = "site.remove",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.additionalCorrespondenceInformation"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
    )

  private def contactNumbersSummaryListRow(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = messages("checkCorrespondenceDetails.heading.contactNumbers")
      ),
      value = Value(content = contactNumbersContent()).withCssClass("contact-numbers"),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href               = "#",
              content            = "site.change",
              visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.contactNumbers"))
            )
          ),
          classes = "govuk-summary-list__actions govuk-!-width-one-third"
        )
      )
    )

  private def addFaxNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    addCorrespondenceFaxNumber map { add =>
      SummaryListRow(
        key = Key(
          content = messages("checkCorrespondenceDetails.heading.addFaxNumber")
        ),
        value = Value(
          content = if (add) {
            messages("site.yes")
          } else {
            messages("site.no")
          }
        ),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.addFaxNumber"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      )
    }

  private def faxNumberSummaryListRow(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = messages("checkCorrespondenceDetails.heading.faxNumber")
      ),
      value = Value(
        content = faxNumber getOrElse messages("checkCorrespondenceDetails.message.notProvided")
      ),
      actions = if (faxNumber.isEmpty) {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.routes.FaxNumberController.onPageLoad(NormalMode).url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.faxNumber"))
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
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.faxNumber"))
              ),
              ActionItem(
                href               = controllers.routes.RemoveFaxNumberController.onPageLoad(NormalMode).url,
                content            = "site.remove",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.faxNumber"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
    )

  private def addEmailAddressSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    addCorrespondenceEmailAddress map { add =>
      SummaryListRow(
        key = Key(
          content = messages("checkCorrespondenceDetails.heading.addEmailAddress")
        ),
        value = Value(
          content = if (add) {
            messages("site.yes")
          } else {
            messages("site.no")
          }
        ),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = "#",
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.addEmailAddress"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      )
    }

  private def emailAddressSummaryListRow(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = messages("checkCorrespondenceDetails.heading.emailAddr")
      ),
      value = Value(
        content = emailAddress getOrElse messages("checkCorrespondenceDetails.message.notProvided")
      ),
      actions = if (emailAddress.isEmpty) {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.routes.ChangeEmailAddressController.onPageLoad(NormalMode).url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.emailAddr"))
              )
            )
          )
        )
      } else {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.routes.ChangeEmailAddressController.onPageLoad(NormalMode).url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.emailAddr"))
              ),
              ActionItem(
                href               = controllers.routes.RemoveEmailAddressController.onPageLoad(NormalMode).url,
                content            = "site.remove",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.emailAddr"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
    )

  private def contactNumbersContent(implicit messages: Messages): Content =
    if (phoneNumber.isEmpty && mobilePhoneNumber.isEmpty) {
      messages("checkCorrespondenceDetails.message.notProvided")
    } else {
      HtmlContent(
        Html(
          s"${messages("checkCorrespondenceDetails.label.phoneNumber")}" +
            "<br>" +
            s"${phoneNumber getOrElse messages("checkCorrespondenceDetails.message.notProvided")}" +
            "<br>" +
            "<br>" +
            s"${messages("checkCorrespondenceDetails.label.mobilePhoneNumber")}" +
            "<br>" +
            s"${mobilePhoneNumber getOrElse messages("checkCorrespondenceDetails.message.notProvided")}"
        )
      )
    }

  private def addressContent(implicit messages: Messages): Content = {
    correspondenceAddress.map { address =>
      HtmlContent(
        Html(
          address.postcode match {
            case Some(_) =>
              Seq(
                Some(address.address1),
                address.address2,
                address.address3,
                address.address4,
                address.postcode
              ).flatten.mkString("<br>")
            case None =>
              Seq(
                Some(address.address1),
                address.address2,
                address.address3,
                address.address4,
                address.country
              ).flatten.mkString("<br>")
          }
        )
      )
    } getOrElse {
      messages("checkCorrespondenceDetails.message.notProvided")
    }
  }

}
