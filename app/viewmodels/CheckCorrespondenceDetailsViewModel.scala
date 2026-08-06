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
import play.api.mvc.Call

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
                                               hasUkPostcode: Option[Boolean],
                                               isSubmitted: Boolean,
                                               isAddingNewCorrespondenceDetails: Option[Boolean]
                                              ) {

  def continueCall: Call =
    if (correspondenceName.isEmpty) {
      controllers.routes.CorrespondenceNameController.onPageLoad()
    } else if (phoneNumber.isEmpty && mobilePhoneNumber.isEmpty) {
      controllers.routes.CorrespondenceContactNumberController.onPageLoad()
    } else {
      controllers.routes.ChangeRegistrationDetailsController.onPageLoad()
    }

  def summaryList(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    Some(correspondenceNameSummaryListRow),
    addAdditionalCorrespondenceNameSummaryListRow,
    additionalCorrespondenceNameSummaryListRow,
    hasUkPostcodeSummaryListRow,
    Some(correspondenceAddressUkSummaryListRow),
    addAdditionalInformationSummaryListRow,
    additionalInformationSummaryListRow,
    Some(contactNumbersSummaryListRow),
    addFaxNumberSummaryListRow,
    faxNumberSummaryListRow,
    addEmailAddressSummaryListRow,
    emailAddressSummaryListRow
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
                href               = controllers.routes.CorrespondenceNameController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.correspondenceName.hidden"))
              )
            )
          )
        )
      } else {
        Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.routes.CorrespondenceNameController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.correspondenceName.hidden"))
              )
            )
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
                href               = controllers.routes.CorrespondenceAdditionalNameYesNoController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.addAdditionalCorrespondenceName.hidden"))
              )
            )
          )
        )
      )
    }

  private def additionalCorrespondenceNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (additionalCorrespondenceName.isEmpty && isAddingNewCorrespondenceDetails.contains(true)) {
      None
    } else {
      Some(
        SummaryListRow(
          key = Key(
            content = messages("checkCorrespondenceDetails.heading.additionalCorrespondenceName")
          ),
          value = Value(
            content = additionalCorrespondenceName.getOrElse(
              messages("checkCorrespondenceDetails.message.notProvided")
            )
          ),
          actions = if (additionalCorrespondenceName.isEmpty) {
            Some(
              Actions(
                items = Seq(
                  ActionItem(
                    href    = controllers.routes.CorrespondenceAdditionalNameController.onPageLoad().url,
                    content = "site.change",
                    visuallyHiddenText = Some(
                      messages("checkCorrespondenceDetails.label.additionalCorrespondenceName.hidden")
                    )
                  )
                )
              )
            )
          } else {
            val items = Seq(
              Some(
                ActionItem(
                  href    = controllers.routes.CorrespondenceAdditionalNameController.onPageLoad().url,
                  content = "site.change",
                  visuallyHiddenText = Some(
                    messages("checkCorrespondenceDetails.label.additionalCorrespondenceName.hidden")
                  )
                )
              ),
              if (!isAddingNewCorrespondenceDetails.contains(true))
                Some(
                  ActionItem(
                    href    = controllers.routes.RemoveAdditionalCorrespondenceNameYesNoController.onPageLoad().url,
                    content = "site.remove",
                    visuallyHiddenText = Some(
                      messages("checkCorrespondenceDetails.label.additionalCorrespondenceName.hidden")
                    )
                  )
                )
              else None
            ).flatten

            Some(Actions(items = items))
          }
        )
      )
    }
  private def correspondenceAddressUkSummaryListRow(implicit messages: Messages): SummaryListRow = {

    val changeUrl =
      if (correspondenceAddress.isEmpty) {
        controllers.routes.CorrespondenceUKAddrScreenerController.onPageLoad().url
      } else if (hasUkPostcode.contains(true)) {
        controllers.routes.CorrespondenceUKAddressController.onPageLoad().url
      } else if (hasUkPostcode.contains(false)) {
        controllers.routes.CorrespondenceNonUKAddressController.onPageLoad().url
      } else {
        controllers.routes.CorrespondenceUKAddrScreenerController.onPageLoad().url
      }

    SummaryListRow(
      key = Key(
        content = messages("checkCorrespondenceDetails.heading.correspondenceAddress")
      ),
      value = Value(
        content = addressContent
      ),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href    = changeUrl,
              content = "site.change",
              visuallyHiddenText = Some(
                messages("checkCorrespondenceDetails.label.correspondenceAddress.hidden")
              )
            )
          )
        )
      )
    )
  }

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
                href               = controllers.routes.CorrespondenceAddrInfoScreenerController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.addAdditionalInformation.hidden"))
              )
            )
          )
        )
      )
    }

  private def additionalInformationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (correspondenceAdditionalInformation.isEmpty && isAddingNewCorrespondenceDetails.contains(true)) {
      None
    } else {
      Some(
        SummaryListRow(
          key = Key(
            content = messages("checkCorrespondenceDetails.heading.additionalCorrespondenceInformation")
          ),
          value = Value(
            content = correspondenceAdditionalInformation.getOrElse(
              messages("checkCorrespondenceDetails.message.notProvided")
            )
          ),
          actions = if (correspondenceAdditionalInformation.isEmpty) {
            Some(
              Actions(
                items = Seq(
                  ActionItem(
                    href    = controllers.routes.CorrespondenceAdditionalInfoController.onPageLoad().url,
                    content = "site.change",
                    visuallyHiddenText = Some(
                      messages("checkCorrespondenceDetails.label.additionalCorrespondenceInformation.hidden")
                    )
                  )
                )
              )
            )
          } else {
            val items = Seq(
              Some(
                ActionItem(
                  href    = controllers.routes.CorrespondenceAdditionalInfoController.onPageLoad().url,
                  content = "site.change",
                  visuallyHiddenText = Some(
                    messages("checkCorrespondenceDetails.label.additionalCorrespondenceInformation.hidden")
                  )
                )
              ),
              if (!isAddingNewCorrespondenceDetails.contains(true))
                Some(
                  ActionItem(
                    href    = controllers.routes.RemoveCorrAddressAddInfoController.onPageLoad().url,
                    content = "site.remove",
                    visuallyHiddenText = Some(
                      messages("checkCorrespondenceDetails.label.additionalCorrespondenceInformation.hidden")
                    )
                  )
                )
              else None
            ).flatten

            Some(Actions(items = items))
          }
        )
      )
    }
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
              href               = controllers.routes.CorrespondenceContactNumberController.onPageLoad().url,
              content            = "site.change",
              visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.contactNumbers.hidden"))
            )
          )
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
                href               = controllers.routes.FaxNumberForCorrespondenceYesNoController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.addFaxNumber.hidden"))
              )
            )
          )
        )
      )
    }

  private def faxNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (faxNumber.isEmpty && isAddingNewCorrespondenceDetails.contains(true)) {
      None
    } else {
      Some(
        SummaryListRow(
          key = Key(
            content = messages("checkCorrespondenceDetails.heading.faxNumber")
          ),
          value = Value(
            content = faxNumber.getOrElse(
              messages("checkCorrespondenceDetails.message.notProvided")
            )
          ),
          actions = if (faxNumber.isEmpty) {
            Some(
              Actions(
                items = Seq(
                  ActionItem(
                    href    = controllers.routes.CorrespondenceFaxNumberController.onPageLoad().url,
                    content = "site.change",
                    visuallyHiddenText = Some(
                      messages("checkCorrespondenceDetails.label.faxNumber.hidden")
                    )
                  )
                )
              )
            )
          } else {
            val items = Seq(
              Some(
                ActionItem(
                  href    = controllers.routes.CorrespondenceFaxNumberController.onPageLoad().url,
                  content = "site.change",
                  visuallyHiddenText = Some(
                    messages("checkCorrespondenceDetails.label.faxNumber.hidden")
                  )
                )
              ),
              if (!isAddingNewCorrespondenceDetails.contains(true))
                Some(
                  ActionItem(
                    href    = controllers.routes.RemoveCorrespondenceFaxNumberController.onPageLoad(NormalMode).url,
                    content = "site.remove",
                    visuallyHiddenText = Some(
                      messages("checkCorrespondenceDetails.label.faxNumber.hidden")
                    )
                  )
                )
              else None
            ).flatten

            Some(Actions(items = items))
          }
        )
      )
    }

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
                href               = controllers.routes.AddEmailAddressForCorrespondenceYesNoController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("checkCorrespondenceDetails.label.addEmailAddress.hidden"))
              )
            )
          )
        )
      )
    }

  private def emailAddressSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (emailAddress.isEmpty && isAddingNewCorrespondenceDetails.contains(true)) {
      None
    } else {
      Some(
        SummaryListRow(
          key = Key(
            content = messages("checkCorrespondenceDetails.heading.emailAddr")
          ),
          value = Value(
            content = emailAddress.getOrElse(
              messages("checkCorrespondenceDetails.message.notProvided")
            )
          ),
          actions = if (emailAddress.isEmpty) {
            Some(
              Actions(
                items = Seq(
                  ActionItem(
                    href    = controllers.routes.CorrespondenceEmailAddressController.onPageLoad().url,
                    content = "site.change",
                    visuallyHiddenText = Some(
                      messages("checkCorrespondenceDetails.label.emailAddr.hidden")
                    )
                  )
                )
              )
            )
          } else {
            val items = Seq(
              Some(
                ActionItem(
                  href    = controllers.routes.CorrespondenceEmailAddressController.onPageLoad().url,
                  content = "site.change",
                  visuallyHiddenText = Some(
                    messages("checkCorrespondenceDetails.label.emailAddr.hidden")
                  )
                )
              ),
              if (!isAddingNewCorrespondenceDetails.contains(true))
                Some(
                  ActionItem(
                    href    = controllers.routes.RemoveCorrespondenceEmailAddressController.onPageLoad().url,
                    content = "site.remove",
                    visuallyHiddenText = Some(
                      messages("checkCorrespondenceDetails.label.emailAddr.hidden")
                    )
                  )
                )
              else None
            ).flatten

            Some(Actions(items = items))
          }
        )
      )
    }
  private def hasUkPostcodeSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    hasUkPostcode map { answer =>
      SummaryListRow(
        key = Key(
          content = messages("correspondenceUKAddrScreener.heading.addAdditionalInformation")
        ),
        value = Value(
          content =
            if (answer) messages("site.yes")
            else messages("site.no")
        ),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.routes.CorrespondenceUKAddrScreenerController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("correspondenceUKAddrScreener.label.addAdditionalInformation.hidden"))
              )
            )
          )
        )
      )
    }

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
