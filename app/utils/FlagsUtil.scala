package utils

import models.UserAnswers
import models.requests.DataRequest
import org.apache.pekko.actor.typed.delivery.internal.ProducerControllerImpl.Request
import pages.QuestionPage
import repositories.SessionRepository

object FlagsUtil {
  def checkFlag(userAnswers: UserAnswers, changesPage: QuestionPage[Boolean], submittedOnlyPage: QuestionPage[Boolean]): Boolean = {
    val changed = userAnswers.get(changesPage).getOrElse(false)
    val submittedOnly = userAnswers.get(submittedOnlyPage).getOrElse(false)
    changed || submittedOnly
  }

  def flagIfChanged[A](
                        value: Any, sessionRepository: SessionRepository,
                        referencePage: QuestionPage[A],
                        changesPage: QuestionPage[Boolean])(implicit request: DataRequest[?]): Boolean = {
    !(value == sessionRepository.get(referencePage)) || request.userAnswers.get(changesPage).getOrElse(false)
  }
}
