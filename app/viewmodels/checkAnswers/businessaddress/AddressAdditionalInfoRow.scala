package viewmodels.checkAnswers.businessaddress

import controllers.routes
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.all.{ActionItemViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*


case class AddressAdditionalInfoRow(isAdded: Boolean)(implicit messages: Messages) {
  def toRow: SummaryListRow = {
    val yesNoValue = ValueViewModel(
      if(isAdded) "site.yes" else "site.no"
    )

    SummaryListRowViewModel(
      key = "checkBusinessAddress.question.addInfo",
      value = yesNoValue,
      actions = Seq(ActionItemViewModel("site.change", routes.PageNotFoundController.onPageLoad().url))
    )
  }
}
