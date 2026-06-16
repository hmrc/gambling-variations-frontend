/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import connectors.GamblingConnector
import controllers.routes
import models.requests.DataRequest
import models.{MgdTradeDetails, UserAnswers}
import pages.*
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

class CheckTradingDetailsDataRequiredActionImpl @Inject() (
  val sessionRepository: SessionRepository,
  val gamblingConnector: GamblingConnector
)(implicit val executionContext: ExecutionContext)
    extends CheckTradingDetailsDataRequiredAction
    with Logging {

  override protected def refine[A](
    request: DataRequest[A]
  ): Future[Either[Result, DataRequest[A]]] = {

    val answers = request.userAnswers

    if (answers.get(MgdTradeDetailsSectionPage).isDefined) {
      Future.successful(Right(request))
    } else {

      given HeaderCarrier =
        HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      populateAndSave(answers, request)
    }
  }

  private def populateAndSave[A](
    answers: UserAnswers,
    request: DataRequest[A]
  )(using HeaderCarrier): Future[Either[Result, DataRequest[A]]] = {

    gamblingConnector
      .getMgdTradeDetails(answers.id)
      .flatMap { details =>

        setMgdTradeDetails(details, answers) match {

          case scala.util.Success(updatedAnswers) =>
            sessionRepository.set(updatedAnswers).map {

              case true =>
                logger.info("MgdTradeDetails successfully saved into UserAnswers")

                Right(
                  request.copy(userAnswers = updatedAnswers)
                )

              case false =>
                logger.error("Failed to persist UserAnswers")
                Left(Redirect(routes.SystemErrorController.onPageLoad()))
            }

          case scala.util.Failure(ex) =>
            logger.error("Failed to map MgdTradeDetails into UserAnswers", ex)
            Future.successful(
              Left(Redirect(routes.SystemErrorController.onPageLoad()))
            )
        }
      }
      .recover { case NonFatal(e) =>
        logger.warn(
          s"Unable to populate CheckTradingDetails for ${request.mgdRegNum}",
          e
        )
        Left(Redirect(routes.SystemErrorController.onPageLoad()))
      }
  }

  private def setMgdTradeDetails(
    details: MgdTradeDetails,
    answers: UserAnswers
  ): Try[UserAnswers] = {
    logger.info("Mapping MgdTradeDetails into UserAnswers")

    val previousRegs =
      details.previousMgdRegistrationNumbers
        .map(_.map(_.trim).filter(_.nonEmpty))
        .filter(_.nonEmpty)

    val associatedRegs =
      details.associatedMgdRegistrationNumbers
        .map(_.map(_.trim).filter(_.nonEmpty))
        .filter(_.nonEmpty)

    for {
      updatedAnswers <- answers.set(MgdTradeDetailsSectionPage, details.mgdRegNumber)
      updatedAnswers <- setIfDefined(updatedAnswers, details.isBusinessSeasonal, IsSeasonalBusinessPage)
      updatedAnswers <- setIfDefined(updatedAnswers, details.businessTradeClass, BusinessTradeClassPage)
      updatedAnswers <- setIfDefined(updatedAnswers, details.businessActivityDesc, OtherTradeClassPage)
      updatedAnswers <- setIfDefined(updatedAnswers, previousRegs, PreviousRegistrationNumbersPage)
      updatedAnswers <- setIfDefined(updatedAnswers, associatedRegs, AssociatedRegistrationNumbersPage)
    } yield updatedAnswers
  }

  private def setIfDefined[A](
    userAnswers: UserAnswers,
    optional: Option[A],
    page: QuestionPage[A]
  )(implicit wrt: play.api.libs.json.Writes[A]): Try[UserAnswers] =
    optional.fold(scala.util.Success(userAnswers)) { value =>
      userAnswers.set(page, value)
    }
}

trait CheckTradingDetailsDataRequiredAction extends ActionRefiner[DataRequest, DataRequest]
