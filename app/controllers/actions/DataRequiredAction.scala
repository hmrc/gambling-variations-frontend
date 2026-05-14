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
import models.{BusinessContactDetails, BusinessNameDetails, BusinessType, SoleProprietorName, SoleProprietorNameDetails, UserAnswers}
import pages.*
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.Try

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

          val answers = UserAnswers(request.mgdRegNum)
          val updatedAnswers: Try[UserAnswers] = entityName match {
            case SoleProprietorNameDetails(_, title, firstName, middleName, lastName, tradingName, _, _) =>
              for {
                a <- answers.set(SoleProprietorPage, SoleProprietorName(title, firstName, middleName, lastName))
                b <- a.set(BusinessTypePage, BusinessType.Soleproprietor)
                c <- setTradingName(b, tradingName)
              } yield c
            case BusinessNameDetails(_, businessName, businessType, tradingName, _) =>
              for {
                a <- answers.set(BusinessNamePage, businessName)
                b <- a.set(BusinessTypePage, businessType)
                c <- setTradingName(b, tradingName)
              } yield c
          }

          gamblingConnector.getBusinessContactDetails(request.mgdRegNum) flatMap { contact =>
            Future(setBusinessContactDetails(updatedAnswers, contact))
          }

          updatedAnswers.map { ua =>
            logger.info("User Answers not found. Saving User Answers")
            sessionRepository.set(ua) map {
              case true =>
                logger.info("User Answers saved.")
                Right(DataRequest(request.request, request.mgdRegNum, ua))
              case false =>
                logger.info("User Answers failed.")
                Left(Redirect(routes.SystemErrorController.onPageLoad()))
            }
          } getOrElse Future.successful(Left(Redirect(routes.SystemErrorController.onPageLoad())))
        } recover { case NonFatal(e) =>
          logger.warn(s"Unable to populate User Answers for id ${request.mgdRegNum}", e)
          Left(Redirect(routes.SystemErrorController.onPageLoad()))
        }
      case Some(data) =>
        logger.info(s"User Answers found with id ${data.id}")
        Future.successful(Right(DataRequest(request.request, request.mgdRegNum, data)))
    }
  }

  private def setTradingName(userAnswers: UserAnswers, tradingName: Option[String]): Try[UserAnswers] =
    tradingName.fold(Try(userAnswers)) { tradingName =>
      userAnswers.set(TradingNamePage, tradingName)
    }

  private def setBusinessContactDetails(updatedA: Try[UserAnswers], contact: BusinessContactDetails): Try[UserAnswers] = {
    val finalAnswers: Try[UserAnswers] = {
      for {
        answers <- updatedA
        a       <- answers.set(PhoneNumberPage, contact.phoneNumber)
        b       <- a.set(MobileNumberPage, contact.mobilePhoneNumber)
        c       <- b.set(FaxNumberPage, contact.faxNumber)
        d       <- c.set(BusinessEmailPage, contact.emailAddr)
      } yield d
    }
    finalAnswers
  }
}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
