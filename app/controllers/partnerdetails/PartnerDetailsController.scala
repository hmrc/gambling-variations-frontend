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

import config.FrontendAppConfig
import controllers.actions.*
import forms.partnerdetails.AddAnotherPartnerFormProvider
import models.NormalMode
import pages.partnerdetails.{PartnerDetailsAddAnotherPartnerYesNoPage, PartnerDetailsChosenPartnerToRemovePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PartnerUtils
import viewmodels.checkAnswers.partnerdetails.PartnerDetailsViewModel
import views.html.partnerdetails.PartnerDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

//TODO this controller has changed, note the route for QA
class PartnerDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: AddAnotherPartnerFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsView,
  frontendAppConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      val viewModel =
        PartnerDetailsViewModel.from(
          request.userAnswers,
          frontendAppConfig
        )

      val errorMessage =
        if (viewModel.showNoPartnersMessage)
          "partnerDetails.addPartner.error.required"
        else
          "partnerDetails.addAnotherPartner.error.required"

      val form = formProvider(errorMessage)

      val preparedForm =
        request.userAnswers
          .get(PartnerDetailsAddAnotherPartnerYesNoPage)
          .fold(form)(form.fill)

      Ok(
        view(
          preparedForm,
          viewModel
        )
      )
    }

  def onSubmit: Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      val viewModel =
        PartnerDetailsViewModel.from(
          request.userAnswers,
          frontendAppConfig
        )

      val errorMessage =
        if (viewModel.showNoPartnersMessage)
          "partnerDetails.addPartner.error.required"
        else
          "partnerDetails.addAnotherPartner.error.required"

      formProvider(errorMessage)
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  viewModel
                )
              )
            ),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(
                    PartnerDetailsAddAnotherPartnerYesNoPage,
                    value
                  )
                )

              _ <- sessionRepository.set(updatedAnswers)
            } yield {
              if (value) {
                Redirect(
                  // TODO, make sure if this logic works as expected
                  controllers.partnerdetails.routes.PartnerDetailsBusinessTypeController.onPageLoad(
                    PartnerUtils.findIndexForNewPartner(updatedAnswers).toString,
                    NormalMode
                  )
                )
              } else {
                Redirect(
                  controllers.routes.ChangeRegistrationDetailsController
                    .onPageLoad()
                )
              }
            }
        )
    }

  def onPartnerDetails(partnerNumber: String): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      Redirect(
        routes.PartnerDetailsController.onPageLoad
      )
    }

  def onRemove(partnerNumber: String): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      for {
        updatedAnswers <-
          Future.fromTry(
            request.userAnswers.set(
              PartnerDetailsChosenPartnerToRemovePage,
              partnerNumber
            )
          )

        _ <- sessionRepository.set(updatedAnswers)

      } yield Redirect(
        controllers.partnerdetails.routes.PartnerDetailsDeleteDateController.onPageLoad()
      )
    }

  def onContinue: Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      Redirect(
        controllers.routes.ChangeRegistrationDetailsController
          .onPageLoad()
      )
    }
}
