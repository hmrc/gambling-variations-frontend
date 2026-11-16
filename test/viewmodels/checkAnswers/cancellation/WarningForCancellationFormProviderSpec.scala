package viewmodels.checkAnswers.cancellation

import base.SpecBase
import controllers.cancellation.routes.WarningForCancellationController
import pages.cancellation.WarningForCancellationPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class WarningForCancellationSummarySpec extends SpecBase {

  lazy val app: Application = applicationBuilder().build()

  implicit val messages: Messages = this.messages(app)

  "WarningForCancellationSummary" - {

    "must return None when the question has not been answered" in {
      WarningForCancellationSummary.row(emptyUserAnswers) mustBe None
    }

    "must return the correct row when the answer is Yes" in {
      val answers =
        emptyUserAnswers
          .set(WarningForCancellationPage, true)
          .success
          .value

      WarningForCancellationSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "warningForCancellation.checkYourAnswersLabel",
          value = ValueViewModel("site.yes"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              WarningForCancellationController.onPageLoad().url
            ).withVisuallyHiddenText(
              messages("warningForCancellation.change.hidden")
            )
          )
        )
    }

    "must return the correct row when the answer is No" in {
      val answers =
        emptyUserAnswers
          .set(WarningForCancellationPage, false)
          .success
          .value

      WarningForCancellationSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "warningForCancellation.checkYourAnswersLabel",
          value = ValueViewModel("site.no"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              WarningForCancellationController.onPageLoad().url
            ).withVisuallyHiddenText(
              messages("warningForCancellation.change.hidden")
            )
          )
        )
    }
  }
}
