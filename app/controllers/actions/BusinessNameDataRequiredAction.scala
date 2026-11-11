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
import models.requests.{DataRequest, OptionalDataRequest}
import models.{BusinessNameDetails, BusinessType, EntityName, SoleProprietorName, SoleProprietorNameDetails, UserAnswers}
import pages.*
import pages.businessname.{BusinessNamePage, BusinessNameSection, SoleProprietorPage, TradingNamePage}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

class BusinessNameDataRequiredActionImpl @Inject() (
  val sessionRepository: SessionRepository,
  val gamblingConnector: GamblingConnector,
  clock: Clock
)(implicit val executionContext: ExecutionContext)
    extends BusinessNameDataRequiredAction
    with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    request.userAnswers match {
      case None =>
        logger.info(s"User Answers not found. Populating User Answers to id ${request.mgdRegNum}")

        given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        val answers = UserAnswers(request.mgdRegNum)
        saveUserAnswersToSessionAndRedirect(answers, request)

      case Some(userAnswers) =>
        logger.info(s"User Answers found with id ${userAnswers.id}")

        if (userAnswers.isLoaded(BusinessNameSection)) {
          logger.info(s"Business Name already loaded for id ${userAnswers.id}")

          Future.successful(Right(DataRequest(request.request, request.mgdRegNum, userAnswers)))
        } else {
          logger.info(s"User Answers found with id ${userAnswers.id}")

          given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
          saveUserAnswersToSessionAndRedirect(userAnswers, request)
        }
    }
  }

  private def saveUserAnswersToSessionAndRedirect[A](answers: UserAnswers, request: OptionalDataRequest[A])(using HeaderCarrier) = {
    gamblingConnector.getBusinessName(answers.id) flatMap { entityName =>

      setBusinessName(entityName, answers) map { updatedAnswers =>
        logger.info("User Answers updated with Business Name. Saving User Answers")
        sessionRepository.set(updatedAnswers) map {
          case true =>
            logger.info("User Answers saved.")
            Right(DataRequest(request.request, request.mgdRegNum, updatedAnswers))
          case false =>
            logger.info("User Answers failed.")
            Left(Redirect(routes.SystemErrorController.onPageLoad()))
        }
      } getOrElse Future.successful(Left(Redirect(routes.SystemErrorController.onPageLoad())))

    } recover { case NonFatal(e) =>
      logger.warn(s"Unable to populate User Answers for id ${request.mgdRegNum}", e)
      Left(Redirect(routes.SystemErrorController.onPageLoad()))
    }
  }

  private def setBusinessName(entity: EntityName, answers: UserAnswers): Try[UserAnswers] = {
    logger.info("Setting User Answers for Business Name")
    val loadedAnswers = answers.withSectionLoaded(BusinessNameSection, Instant.now(clock))
    // The business type belongs to Business Details, which loads it as well, so its path is outside this section.
    // It is loaded here too, since the business name pages depend on it.
    entity match {
      case SoleProprietorNameDetails(_, title, firstName, middleName, lastName, tradingName, _, _) =>
        for {
          updatedAnswers <- loadedAnswers.setOriginal(SoleProprietorPage, SoleProprietorName(title, firstName, middleName, lastName))
          updatedAnswers <- updatedAnswers.setOriginal(BusinessTypePage, BusinessType.Soleproprietor)
          updatedAnswers <- updatedAnswers.setOriginalIfDefined(TradingNamePage, tradingName)
        } yield updatedAnswers
      case BusinessNameDetails(_, businessName, businessType, tradingName, _) =>
        for {
          updatedAnswers <- loadedAnswers.setOriginal(BusinessNamePage, businessName)
          updatedAnswers <- updatedAnswers.setOriginal(BusinessTypePage, businessType)
          updatedAnswers <- updatedAnswers.setOriginalIfDefined(TradingNamePage, tradingName)
        } yield updatedAnswers
    }
  }

}

trait BusinessNameDataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
