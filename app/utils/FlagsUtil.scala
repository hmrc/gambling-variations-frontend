package utils

import models.UserAnswers
import pages.QuestionPage

object FlagsUtil {
  def checkFlag[A](userAnswers: UserAnswers, changesPage: QuestionPage[Boolean], submittedOnlyPage: QuestionPage[Boolean]): Boolean = {
    val changed = userAnswers.get(changesPage).getOrElse(false)
    val submittedOnly = userAnswers.get(submittedOnlyPage).getOrElse(false)
    changed || submittedOnly
  }
}
