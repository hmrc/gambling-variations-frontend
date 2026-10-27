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

package controllers.partnerdetails

import controllers.actions.*
import controllers.routes
import models.UserAnswers
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsHasAnsweredAllQuestionsPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.partnerdetails.CheckPartnerDetailsViewModel.from
import viewmodels.checkAnswers.partnerdetails.Partner.indexParser
import viewmodels.checkAnswers.partnerdetails.{CheckPartnerDetailsViewModel, ExistingPartner, NewPartner, PartnerMandatoryDetails}
import views.html.partnerdetails.PartnerDetailsCheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerDetailsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsCheckYourAnswersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(index: String): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      val answers: UserAnswers = request.userAnswers
      createPartner(index, answers)
        .fold(
          error => {
            logger.warn(error)
            Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
          },
          partner =>
            if (partner.isNewPartnerFlow)
              for {
                updatedAnswers <- Future.fromTry(answers.set(PartnerDetailsHasAnsweredAllQuestionsPage(index.toInt), true))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Ok(view(partner))
            else
              Future.successful(Ok(view(partner)))
        )
    }

  def onContinue(index: String): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      val answers = request.userAnswers
      val partnerDetailsList = Redirect(controllers.partnerdetails.routes.PartnerDetailsController.onPageLoad(None))

      indexParser(index, answers)
        .fold(
          error => {
            logger.warn(error)
            Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
          },
          {
            case NewPartner(partnerIndex, _) =>
              PartnerMandatoryDetails.firstMissing(answers, partnerIndex) match {
                case Some(missing) =>
                  Future.successful(Redirect(missing.call))
                case None =>
                  for {
                    updatedAnswers <- Future.fromTry(answers.set(PartnerDetailsAddPartnerCompletedPage(partnerIndex), true))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield partnerDetailsList
              }

            case ExistingPartner(_) =>
              Future.successful(partnerDetailsList)
          }
        )
    }

  private def createPartner(index: String, answers: UserAnswers): Either[String, CheckPartnerDetailsViewModel] =
    indexParser(index, answers).map(partner => from(answers, partner.ref, partner.isNew, partner.isSubmitted))

}
