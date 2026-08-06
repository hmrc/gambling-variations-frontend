package viewmodels.checkAnswers.businessAddress

import models.UserAnswers
import controllers.routes
import pages.BusinessAddressUkPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class BusinessAddressUKSummary {
  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(BusinessAddressUkPage).map { answer =>
      SummaryListRowViewModel(
        key = "checkBusinessAddress.label.address",
        value = ValueViewModel(HtmlFormat.escape(answer).toString),
        actions = Seq(
          if (answer != null) {
            ActionItemViewModel("site.change", routes.PageNotFoundController.onPageLoad().url)
            ActionItemViewModel("site.remove", routes.PageNotFoundController.onPageLoad().url)
          } else {
            ActionItemViewModel("site.change", routes.PageNotFoundController.onPageLoad().url)
          }
        )
      )
  }
}
