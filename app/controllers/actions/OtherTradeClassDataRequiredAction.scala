package controllers.actions

import controllers.routes
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.Logging
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Redirect

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherTradeClassDataRequiredActionImpl @Inject() (
                                                      )(implicit val executionContext: ExecutionContext)
  extends OtherTradeClassDataRequiredAction
    with Logging {

  override protected def refine[A](
                                    request: OptionalDataRequest[A]
                                  ): Future[Either[Result, DataRequest[A]]] = {

    request.userAnswers match {

      case None =>
        logger.warn(s"No UserAnswers found for id ${request.mgdRegNum}. Redirecting to SystemError.")
        Future.successful(Left(Redirect(routes.SystemErrorController.onPageLoad())))

      case Some(answers) =>
        logger.info(s"UserAnswers found for id ${answers.id}. Proceeding.")
        Future.successful(Right(DataRequest(request.request, request.mgdRegNum, answers)))
    }
  }
}

trait OtherTradeClassDataRequiredAction
  extends ActionRefiner[OptionalDataRequest, DataRequest]
