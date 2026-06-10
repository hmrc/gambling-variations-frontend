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
import models.{BusinessDetails, UserAnswers}
import pages.*
import play.api.Logging
import play.api.libs.json.Writes
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

class BusinessDetailsDataRequiredActionImpl @Inject() (
  val sessionRepository: SessionRepository,
  val gamblingConnector: GamblingConnector
)(implicit val executionContext: ExecutionContext)
    extends BusinessDetailsDataRequiredAction
    with Logging {

  override protected def refine[A](
    request: OptionalDataRequest[A]
  ): Future[Either[Result, DataRequest[A]]] = {

    request.userAnswers match {

      case None =>
        logger.info(s"User Answers not found. Populating for id ${request.mgdRegNum}")

        given HeaderCarrier =
          HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        val emptyAnswers = UserAnswers(request.mgdRegNum)

        populateAndSave(emptyAnswers, request)

      case Some(userAnswers) =>
        logger.info(s"User Answers found with id ${userAnswers.id}")

        userAnswers.get(BusinessTypePage) map { _ =>
          Future.successful(
            Right(DataRequest(request.request, request.mgdRegNum, userAnswers))
          )
        } getOrElse {
          given HeaderCarrier =
            HeaderCarrierConverter.fromRequestAndSession(request, request.session)

          populateAndSave(userAnswers, request)
        }
    }
  }

  private def populateAndSave[A](
    answers: UserAnswers,
    request: OptionalDataRequest[A]
  )(using HeaderCarrier): Future[Either[Result, DataRequest[A]]] = {

    gamblingConnector
      .getBusinessDetails(answers.id)
      .flatMap { details =>

        setBusinessDetails(details, answers) match {

          case scala.util.Success(updatedAnswers) =>
            sessionRepository.set(updatedAnswers).map {

              case true =>
                logger.info("BusinessDetails successfully saved into UserAnswers")

                Right(
                  DataRequest(
                    request.request,
                    request.mgdRegNum,
                    updatedAnswers
                  )
                )

              case false =>
                logger.error("Failed to persist UserAnswers")
                Left(Redirect(routes.SystemErrorController.onPageLoad()))
            }

          case scala.util.Failure(ex) =>
            logger.error("Failed to map BusinessDetails into UserAnswers", ex)
            Future.successful(
              Left(Redirect(routes.SystemErrorController.onPageLoad()))
            )
        }
      }
      .recover { case NonFatal(e) =>
        logger.warn(
          s"Unable to populate BusinessDetails for ${request.mgdRegNum}",
          e
        )
        Left(Redirect(routes.SystemErrorController.onPageLoad()))
      }
  }

  private def setIfDefined[A](
    userAnswers: UserAnswers,
    optional: Option[A],
    page: QuestionPage[A]
  )(implicit wrt: Writes[A]): Try[UserAnswers] =
    optional.fold(Try(userAnswers)) { value =>
      userAnswers.set(page, value)
    }

  private def setBusinessDetails(
    details: BusinessDetails,
    answers: UserAnswers
  ): Try[UserAnswers] = {

    logger.info("Mapping BusinessDetails into UserAnswers")

    for {
      updatedAnswers <- setIfDefined(
                          answers,
                          details.businessType,
                          BusinessTypePage
                        )

      updatedAnswers <- updatedAnswers.set(
                          GroupMemberPage,
                          details.groupReg
                        )
    } yield updatedAnswers
  }
}

trait BusinessDetailsDataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
