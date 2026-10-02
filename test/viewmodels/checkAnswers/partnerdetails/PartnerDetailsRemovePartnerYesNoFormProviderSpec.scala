package viewmodels.checkAnswers.partnerdetails

import base.SpecBase
import controllers.routes.PartnerDetailsRemovePartnerYesNoController
import pages.partnerdetails.PartnerDetailsRemovePartnerYesNoPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class PartnerDetailsRemovePartnerYesNoSummarySpec extends SpecBase {

  lazy val app: Application = applicationBuilder().build()

  implicit val messages: Messages = this.messages(app)

  "PartnerDetailsRemovePartnerYesNoSummary" - {

    "must return None when the question has not been answered" in {
      PartnerDetailsRemovePartnerYesNoSummary.row(emptyUserAnswers) mustBe None
    }

    "must return the correct row when the answer is Yes" in {
      val answers =
        emptyUserAnswers
          .set(PartnerDetailsRemovePartnerYesNoPage, true)
          .success
          .value

      PartnerDetailsRemovePartnerYesNoSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "partnerDetailsRemovePartnerYesNo.checkYourAnswersLabel",
          value = ValueViewModel("site.yes"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              PartnerDetailsRemovePartnerYesNoController.onPageLoad().url
            ).withVisuallyHiddenText(
              messages("partnerDetailsRemovePartnerYesNo.change.hidden")
            )
          )
        )
    }

    "must return the correct row when the answer is No" in {
      val answers =
        emptyUserAnswers
          .set(PartnerDetailsRemovePartnerYesNoPage, false)
          .success
          .value

      PartnerDetailsRemovePartnerYesNoSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "partnerDetailsRemovePartnerYesNo.checkYourAnswersLabel",
          value = ValueViewModel("site.no"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              PartnerDetailsRemovePartnerYesNoController.onPageLoad().url
            ).withVisuallyHiddenText(
              messages("partnerDetailsRemovePartnerYesNo.change.hidden")
            )
          )
        )
    }
  }
}
