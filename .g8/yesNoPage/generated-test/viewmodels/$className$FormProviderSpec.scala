package viewmodels

import base.SpecBase
import controllers.routes.$className$Controller
import pages.$className$Page
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class $className$SummarySpec extends SpecBase {

  lazy val app: Application = applicationBuilder().build()

  implicit val messages: Messages = this.messages(app)

  "$className$Summary" - {

    "must return None when the question has not been answered" in {
      $className$Summary.row(emptyUserAnswers) mustBe None
    }

    "must return the correct row when the answer is Yes" in {
      val answers =
        emptyUserAnswers
          .set($className$Page, true)
          .success
          .value

      $className$Summary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "$className;format="decap"$.checkYourAnswersLabel",
      value = ValueViewModel("site.yes"),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          $className$Controller.onPageLoad().url
        ).withVisuallyHiddenText(
          messages("$className;format="decap"$.change.hidden")
      )
      )
      )
    }

    "must return the correct row when the answer is No" in {
      val answers =
        emptyUserAnswers
          .set($className$Page, false)
          .success
          .value

      $className$Summary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "$className;format="decap"$.checkYourAnswersLabel",
      value = ValueViewModel("site.no"),
      actions = Seq(
        ActionItemViewModel(
          "site.change",
          $className$Controller.onPageLoad().url
        ).withVisuallyHiddenText(
          messages("$className;format="decap"$.change.hidden")
      )
      )
      )
    }
  }
}
