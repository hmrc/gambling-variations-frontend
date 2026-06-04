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
import models.{BusinessContactDetails, BusinessContactNumber, BusinessNameDetails, BusinessType, EntityName, MgdTradeDetails, SoleProprietorName, SoleProprietorNameDetails, UserAnswers}
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

class DataRequiredActionImpl @Inject() (
  val sessionRepository: SessionRepository,
  val gamblingConnector: GamblingConnector
)(implicit val executionContext: ExecutionContext)
    extends DataRequiredAction
    with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    request.userAnswers match {
      case None =>
        logger.info(s"User Answers not found. Populating User Answers to id ${request.mgdRegNum}")

        given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        gamblingConnector.getBusinessName(request.mgdRegNum) flatMap { entityName =>
          gamblingConnector.getBusinessContactDetails(request.mgdRegNum) flatMap { contact =>
            gamblingConnector.getMgdTradeDetails(request.mgdRegNum) flatMap { mgdContactDetails =>

              val answers = UserAnswers(request.mgdRegNum)

              (for {
                updatedAnswers <- setBusinessName(entityName, answers)
                updatedAnswers <- setBusinessContactDetails(contact, updatedAnswers)
                updatedAnswers <- setMgdTradeDetails(mgdContactDetails, updatedAnswers)
              } yield {
                logger.info("User Answers not found. Saving User Answers")
                sessionRepository.set(updatedAnswers) map {
                  case true =>
                    logger.info("User Answers saved.")
                    Right(DataRequest(request.request, request.mgdRegNum, updatedAnswers))
                  case false =>
                    logger.info("User Answers failed.")
                    Left(Redirect(routes.SystemErrorController.onPageLoad()))
                }
              }) getOrElse Future.successful(Left(Redirect(routes.SystemErrorController.onPageLoad())))

            }
          }
        } recover { case NonFatal(e) =>
          logger.warn(s"Unable to populate User Answers for id ${request.mgdRegNum}", e)
          Left(Redirect(routes.SystemErrorController.onPageLoad()))
        }
      case Some(data) =>
        logger.info(s"User Answers found with id ${data.id}")
        Future.successful(Right(DataRequest(request.request, request.mgdRegNum, data)))
    }
  }

  private def setIfDefined[A](userAnswers: UserAnswers, optional: Option[A], page: QuestionPage[A])(implicit wrt: Writes[A]): Try[UserAnswers] =
    optional.fold(Try(userAnswers)) { value =>
      userAnswers.set(page, value)
    }

  private def setBusinessName(entity: EntityName, answers: UserAnswers): Try[UserAnswers] = {
    logger.info("Setting User Answers for Business Name")
    entity match {
      case SoleProprietorNameDetails(_, title, firstName, middleName, lastName, tradingName, _, _) =>
        for {
          updatedAnswers <- answers.set(SoleProprietorPage, SoleProprietorName(title, firstName, middleName, lastName))
          updatedAnswers <- updatedAnswers.set(BusinessTypePage, BusinessType.Soleproprietor)
          updatedAnswers <- setIfDefined(updatedAnswers, tradingName, TradingNamePage)
        } yield updatedAnswers
      case BusinessNameDetails(_, businessName, businessType, tradingName, _) =>
        for {
          updatedAnswers <- answers.set(BusinessNamePage, businessName)
          updatedAnswers <- updatedAnswers.set(BusinessTypePage, businessType)
          updatedAnswers <- setIfDefined(updatedAnswers, tradingName, TradingNamePage)
        } yield updatedAnswers
    }
  }

  private def setBusinessContactDetails(contact: BusinessContactDetails, answers: UserAnswers): Try[UserAnswers] = {
    logger.info("Setting User Answers for Business Contact Details")
    for {
      updatedAnswers <- setIfDefined(
                          answers,
                          contact.phoneNumber.zip(contact.mobilePhoneNumber).map { case (phone, mobile) =>
                            BusinessContactNumber(Some(phone), Some(mobile))
                          },
                          BusinessContactNumberPage
                        )
      updatedAnswers <- setIfDefined(updatedAnswers, contact.faxNumber, FaxNumberPage)
      updatedAnswers <- setIfDefined(updatedAnswers, contact.emailAddr, BusinessEmailAddressPage)
    } yield updatedAnswers
  }

  private def setMgdTradeDetails(mgdTradeDetails: MgdTradeDetails, answers: UserAnswers): Try[UserAnswers] = {
    logger.info("Setting User Answers for Mgd Trade Details")
    for {
      updatedAnswers <- setIfDefined(answers, mgdTradeDetails.isBusinessSeasonal, IsSeasonalBusinessPage)
      updatedAnswers <- setIfDefined(updatedAnswers, mgdTradeDetails.businessTradeClass, BusinessTradeClassPage)
      updatedAnswers <- setIfDefined(updatedAnswers, mgdTradeDetails.businessActivityDesc, OtherTradeClassPage)
      updatedAnswers <- setIfDefined(updatedAnswers, mgdTradeDetails.previousMgdRegistrationNumbers, PreviousRegistrationNumbersPage)
      updatedAnswers <- setIfDefined(updatedAnswers, mgdTradeDetails.associatedMgdRegistrationNumbers, AssociatedRegistrationNumbersPage)
    } yield updatedAnswers
  }
}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
