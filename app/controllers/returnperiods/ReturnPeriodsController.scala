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

package controllers.returnperiods

import controllers.actions.*
import controllers.routes
import forms.returnperiods.NonStandardReturnPeriodsFormProvider
import models.{GamblingReturnPeriods, Mode, NonStandardReturnPeriodsOptions, UserAnswers}
import navigation.Navigator
import pages.returnperiods.{GamblingReturnPeriodsPage, NonStandardReturnPeriodsPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returnperiods.NonStandardReturnPeriodsViewModel
import views.html.returnperiods.ReturnPeriodsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReturnPeriodsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: GamblingReturnPeriodsDataRequiredAction,
  formProvider: NonStandardReturnPeriodsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnPeriodsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[NonStandardReturnPeriodsOptions] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    getNonStandardReturnPeriodsViewModel(request.userAnswers).fold(
      Redirect(routes.SystemErrorController.onPageLoad())
    )(viewModel => {
      val preparedForm = request.userAnswers.get(NonStandardReturnPeriodsPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, viewModel, mode))
    })
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    getNonStandardReturnPeriodsViewModel(request.userAnswers).fold(
      Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
    )(viewModel =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, viewModel, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(NonStandardReturnPeriodsPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(NonStandardReturnPeriodsPage, mode, updatedAnswers))
        )
    )
  }

  private def getNonStandardReturnPeriodsViewModel(userAnswers: UserAnswers)(implicit messages: Messages): Option[NonStandardReturnPeriodsViewModel] =
    for {
      gamblingReturnPeriods <- userAnswers.get(GamblingReturnPeriodsPage)
      viewModel             <- NonStandardReturnPeriodsViewModel.from(gamblingReturnPeriods)
    } yield viewModel
}
