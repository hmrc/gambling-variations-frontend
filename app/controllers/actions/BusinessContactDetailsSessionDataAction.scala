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
import models.requests.{DataRequest, OptionalDataRequest, SessionDataRequest}
import models.{BusinessContactDetails, ContactNumber, UserAnswers}
import pages.*
import pages.contactdetails.{BusinessContactDetailsSectionPage, BusinessContactNumberPage, BusinessEmailAddressPage, BusinessFaxNumberPage}
import play.api.Logging
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Redirect
import repositories.{SessionRepository, SubmissionRepository}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

class BusinessContactDetailsSessionDataActionImpl @Inject() (
  val submissionRepository: SubmissionRepository
)(implicit val executionContext: ExecutionContext)
    extends BusinessContactDetailsSessionDataAction
    with Logging {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, SessionDataRequest[A]]] = {

    submissionRepository.get(request.userAnswers.id) flatMap { sessionAnswers =>

      sessionAnswers map { session =>
        val x = setBusinessContactDetails(session, request.userAnswers)

        x map { answers =>
          Future.successful(Right(SessionDataRequest(request.request, request.mgdRegNum, answers, Some(session))))
        } getOrElse Future.successful(Right(SessionDataRequest(request.request, request.mgdRegNum, request.userAnswers, None)))

      } getOrElse Future.successful(Right(SessionDataRequest(request.request, request.mgdRegNum, request.userAnswers, None)))

    } recover { case NonFatal(e) =>
      logger.warn(s"Unable to populate User Answers for id ${request.mgdRegNum}", e)
      Right(SessionDataRequest(request.request, request.mgdRegNum, request.userAnswers, None))
    }
  }

  private def setBusinessContactDetails(sessionAnswers: UserAnswers, submittedAnswers: UserAnswers): Try[UserAnswers] = {
    logger.info("Setting User Answers for Business Contact Details")
    for {
      updatedAnswers <- submittedAnswers.setIfDefined(BusinessFaxNumberPage, sessionAnswers.get(BusinessFaxNumberPage))
      updatedAnswers <- updatedAnswers.setIfDefined(BusinessEmailAddressPage, sessionAnswers.get(BusinessEmailAddressPage))
    } yield updatedAnswers
  }

}

trait BusinessContactDetailsSessionDataAction extends ActionRefiner[DataRequest, SessionDataRequest]
