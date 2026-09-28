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
import models.BusinessType.Soleproprietor
import models.UserAnswers
import models.controllingbody.ControllingBodyDetails
import models.requests.{DataRequest, OptionalDataRequest}
import pages.controllingbody.*
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

class ControllingBodyNameDataRequiredActionImpl @Inject() (
  sessionRepository: SessionRepository,
  gamblingConnector: GamblingConnector
)(implicit val executionContext: ExecutionContext)
    extends ControllingBodyNameDataRequiredAction
    with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    val answers = request.userAnswers.getOrElse(UserAnswers(request.mgdRegNum))
    answers.get(ControllingBodyDetailsLoadedPage) match {
      case Some(true) =>
        Future.successful(Right(DataRequest(request.request, request.mgdRegNum, answers)))
      case _ =>
        given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        (for {
          details        <- gamblingConnector.getControllingBodyDetails(request.mgdRegNum)
          updatedAnswers <- Future.fromTry(populate(answers, details))
          saved          <- sessionRepository.set(updatedAnswers)
        } yield {
          saved match {
            case true  => Right(DataRequest(request.request, request.mgdRegNum, updatedAnswers))
            case false => Left(Redirect(routes.SystemErrorController.onPageLoad()))
          }
        }).recover { case NonFatal(e) =>
          logger.warn("Unable to load controlling body details", e)
          Left(Redirect(routes.SystemErrorController.onPageLoad()))
        }
    }
  }

  private def populate(answers: UserAnswers, details: ControllingBodyDetails): Try[UserAnswers] = {
    val businessType = answers.get(ControllingBodyBusinessTypePage).getOrElse(details.typeOfControllingBody)
    // Preserve names already edited in this session when initially loading the section.
    for {
      withType <- answers.set(ControllingBodyBusinessTypePage, businessType)
      withName <- businessType match {
                    case Soleproprietor =>
                      withType.setIfDefined(ControllingBodySoleProprietorPage,
                                            answers.get(ControllingBodySoleProprietorPage).orElse(details.soleProprietorName)
                                           )
                    case _ =>
                      withType.setIfDefined(ControllingBodyBusinessNamePage,
                                            answers.get(ControllingBodyBusinessNamePage).orElse(details.businessName)
                                           )
                  }
      loaded <- withName.set(ControllingBodyDetailsLoadedPage, true)
    } yield loaded
  }
}

trait ControllingBodyNameDataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
