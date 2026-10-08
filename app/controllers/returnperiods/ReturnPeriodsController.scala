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
import forms.returnperiods.ReturnPeriodsFormProvider
import models.{GamblingReturnPeriods, Mode}
import navigation.Navigator
import pages.GamblingReturnPeriodsPage
import pages.partnerdetails.PartnerDetailsMgdRegNumberPage
import pages.returnperiods.ReturnPeriodsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returnperiods.ReturnPeriodsViewModel
import views.html.returnperiods.ReturnPeriodsView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ReturnPeriod(
  pastPeriods: Seq[LocalDate],
  futurePeriods: Seq[LocalDate]
)

class ReturnPeriodsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: GamblingReturnPeriodsDataRequiredAction,
  formProvider: ReturnPeriodsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnPeriodsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()
  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    (for {
      gamblingReturnPeriods <- request.userAnswers.get(GamblingReturnPeriodsPage)
      viewModel             <- ReturnPeriodsViewModel.from(gamblingReturnPeriods)
      preparedForm = request.userAnswers.get(ReturnPeriodsPage) match {
                       case None        => form
                       case Some(value) => form.fill(value)
                     }
    } yield Ok(view(preparedForm, viewModel, mode))).fold(
      Redirect(routes.SystemErrorController.onPageLoad())
    )(identity)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, ???, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ReturnPeriodsPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ReturnPeriodsPage, mode, updatedAnswers))
      )
  }
}
