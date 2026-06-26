package utils

import javax.inject.Inject
import models.{Mode, UserAnswers}
import models.requests.DataRequest
import navigation.Navigator
import pages.QuestionPage
import play.api.mvc.Results.Redirect
import repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class FlagsUtil @Inject()(
                                sessionRepository: SessionRepository,
                                navigator: Navigator
                              )(implicit ec: ExecutionContext){
    def checkAndSet[A](
        value: Any,
        mode: Mode,
        updateUserAnswers: () => Try[UserAnswers],
        userAnswers: UserAnswers,
        referencePage: QuestionPage[A],
        changesPage: QuestionPage[Boolean],
        submittedOnlyPage: QuestionPage[Boolean]): Unit = {
          if(!(value == sessionRepository.get(referencePage))) {
            for {
              updatedAnswers <- Future.fromTry(updateUserAnswers())
              updatedAnswers <- Future.fromTry(updatedAnswers.set(changesPage, true))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(referencePage, mode, updatedAnswers))
          } else {
            for {
              updatedAnswers <- Future.fromTry(updateUserAnswers())
              updatedAnswers <- Future.fromTry(updatedAnswers.set(submittedOnlyPage, true))
              _ <- sessionRepository.set(updatedAnswers)
            } Redirect(navigator.nextPage(referencePage, mode, updatedAnswers))
          }
    }
}
