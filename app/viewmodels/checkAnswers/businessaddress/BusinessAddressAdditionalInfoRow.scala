package viewmodels.checkAnswers.businessaddress

import models.UserAnswers
import pages.{AddBusinessAddressAdditionalInformationPage, BusinessAddressAdditionalInformationPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.all.{ActionItemViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*

case object BusinessAddressAdditionalInfoRow {
  def from(ua: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    SummaryListRowViewModel(
      key = "checkBusinessAddress.question.addInfo",
      value = ValueViewModel(ua.get(BusinessAddressAdditionalInformationPage) match {
        case Some(addInfo) => addInfo
        case _             => "site.notProvided"
      }),
      actions = Seq(
        ActionItemViewModel("site.change", "#"),
        ActionItemViewModel("site.remove", "#"),
    ))
  }
}
