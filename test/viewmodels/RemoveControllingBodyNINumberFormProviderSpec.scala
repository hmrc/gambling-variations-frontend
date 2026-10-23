package viewmodels

import base.SpecBase
import controllers.routes.RemoveControllingBodyNINumberController
import pages.RemoveControllingBodyNINumberPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class RemoveControllingBodyNINumberSummarySpec extends SpecBase {

  lazy val app: Application = applicationBuilder().build()

  implicit val messages: Messages = this.messages(app)

  "RemoveControllingBodyNINumberSummary" - {

    "must return None when the question has not been answered" in {
      RemoveControllingBodyNINumberSummary.row(emptyUserAnswers) mustBe None
    }

    "must return the correct row when the answer is Yes" in {
      val answers =
        emptyUserAnswers
          .set(RemoveControllingBodyNINumberPage, true)
          .success
          .value

      RemoveControllingBodyNINumberSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "removeControllingBodyNINumber.checkYourAnswersLabel",
      value = ValueViewModel("site.yes"),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          RemoveControllingBodyNINumberController.onPageLoad().url
        ).withVisuallyHiddenText(
          messages("removeControllingBodyNINumber.change.hidden")
      )
      )
      )
    }

    "must return the correct row when the answer is No" in {
      val answers =
        emptyUserAnswers
          .set(RemoveControllingBodyNINumberPage, false)
          .success
          .value

      RemoveControllingBodyNINumberSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "removeControllingBodyNINumber.checkYourAnswersLabel",
      value = ValueViewModel("site.no"),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          RemoveControllingBodyNINumberController.onPageLoad().url
        ).withVisuallyHiddenText(
          messages("removeControllingBodyNINumber.change.hidden")
      )
      )
      )
    }
  }
}
