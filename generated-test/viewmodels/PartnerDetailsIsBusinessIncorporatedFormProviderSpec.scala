package viewmodels

import base.SpecBase
import controllers.routes.PartnerDetailsIsBusinessIncorporatedController
import pages.PartnerDetailsIsBusinessIncorporatedPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class PartnerDetailsIsBusinessIncorporatedSummarySpec extends SpecBase {

  lazy val app: Application = applicationBuilder().build()

  implicit val messages: Messages = this.messages(app)

  "PartnerDetailsIsBusinessIncorporatedSummary" - {

    "must return None when the question has not been answered" in {
      PartnerDetailsIsBusinessIncorporatedSummary.row(emptyUserAnswers) mustBe None
    }

    "must return the correct row when the answer is Yes" in {
      val answers =
        emptyUserAnswers
          .set(PartnerDetailsIsBusinessIncorporatedPage, true)
          .success
          .value

      PartnerDetailsIsBusinessIncorporatedSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "partnerDetailsIsBusinessIncorporated.checkYourAnswersLabel",
      value = ValueViewModel("site.yes"),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          PartnerDetailsIsBusinessIncorporatedController.onPageLoad().url
        ).withVisuallyHiddenText(
          messages("partnerDetailsIsBusinessIncorporated.change.hidden")
      )
      )
      )
    }

    "must return the correct row when the answer is No" in {
      val answers =
        emptyUserAnswers
          .set(PartnerDetailsIsBusinessIncorporatedPage, false)
          .success
          .value

      PartnerDetailsIsBusinessIncorporatedSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "partnerDetailsIsBusinessIncorporated.checkYourAnswersLabel",
      value = ValueViewModel("site.no"),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          PartnerDetailsIsBusinessIncorporatedController.onPageLoad().url
        ).withVisuallyHiddenText(
          messages("partnerDetailsIsBusinessIncorporated.change.hidden")
      )
      )
      )
    }
  }
}
