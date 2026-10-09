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
import forms.returnperiods.{NonStandardReturnPeriodsFormProvider, StandardReturnPeriodsFormProvider}
import models.requests.DataRequest
import models.{GamblingReturnPeriods, Mode, NonStandardReturnPeriodsOptions, StandardReturnPeriodsOptions, UserAnswers}
import navigation.Navigator
import pages.returnperiods.{GamblingReturnPeriodsPage, NonStandardReturnPeriodsPage, StandardReturnPeriodsPage}
import play.api.data.{Form, FormBinding}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returnperiods.NonStandardReturnPeriodsViewModel
import views.html.returnperiods.{NonStandardReturnPeriodsView, StandardReturnPeriodsView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReturnPeriodsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: GamblingReturnPeriodsDataRequiredAction,
  nonStandardPeriodFormProvider: NonStandardReturnPeriodsFormProvider,
  standardPeriodFormProvider: StandardReturnPeriodsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  nonStandardReturnPeriodsView: NonStandardReturnPeriodsView,
  standardReturnPeriodsView: StandardReturnPeriodsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val standardPeriodsForm: Form[NonStandardReturnPeriodsOptions] = nonStandardPeriodFormProvider()
  val nonStandardPeriodsForm: Form[StandardReturnPeriodsOptions] = standardPeriodFormProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    isStandardReturnsPeriod(request.userAnswers).fold(
      Redirect(routes.SystemErrorController.onPageLoad())
    )(isStandardReturnsPeriod =>
      if isStandardReturnsPeriod then standardPeriodOnPageLoad(mode)
      else nonStandardPeriodOnPageLoad(mode)
    )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    isStandardReturnsPeriod(request.userAnswers).fold(
      Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
    )(isStandardReturnsPeriod =>
      if isStandardReturnsPeriod then standardPeriodOnSubmit(mode)
      else nonStandardPeriodOnSubmit(mode)
    )
  }

  private def standardPeriodOnPageLoad(mode: Mode)(implicit request: DataRequest[AnyContent]) = {
    val preparedForm =
      request.userAnswers.get(StandardReturnPeriodsPage) match {
        case None        => nonStandardPeriodsForm
        case Some(value) => nonStandardPeriodsForm.fill(value)
      }

    val returnPeriodsId =
      request.userAnswers
        .get(GamblingReturnPeriodsPage)
        .flatMap(_.returnPeriodsId)
        .map(_.toString)

    Ok(standardReturnPeriodsView(preparedForm, mode, returnPeriodsId))
  }

  private def standardPeriodOnSubmit(mode: Mode)(implicit request: DataRequest[AnyContent]) = {
    nonStandardPeriodsForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful {
            val returnPeriodsId =
              request.userAnswers
                .get(GamblingReturnPeriodsPage)
                .flatMap(_.returnPeriodsId)
                .map(_.toString)

            BadRequest(standardReturnPeriodsView(formWithErrors, mode, returnPeriodsId))
          },
        value =>
          for {
            updatedAnswers <- Future.fromTry(
                                request.userAnswers.set(StandardReturnPeriodsPage, value)
                              )
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(
            navigator.nextPage(
              StandardReturnPeriodsPage,
              mode,
              updatedAnswers
            )
          )
      )

  }

  private def nonStandardPeriodOnPageLoad(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    getNonStandardReturnPeriodsViewModel(request.userAnswers).fold(
      Redirect(routes.SystemErrorController.onPageLoad())
    )(viewModel => {
      val preparedForm = request.userAnswers.get(NonStandardReturnPeriodsPage) match {
        case None        => standardPeriodsForm
        case Some(value) => standardPeriodsForm.fill(value)
      }
      Ok(nonStandardReturnPeriodsView(preparedForm, viewModel, mode))
    })

  private def nonStandardPeriodOnSubmit(mode: Mode)(implicit request: DataRequest[AnyContent]) =
    getNonStandardReturnPeriodsViewModel(request.userAnswers).fold(
      Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
    )(viewModel =>
      standardPeriodsForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(nonStandardReturnPeriodsView(formWithErrors, viewModel, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(NonStandardReturnPeriodsPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(NonStandardReturnPeriodsPage, mode, updatedAnswers))
        )
    )

  private def isStandardReturnsPeriod(userAnswers: UserAnswers): Option[Boolean] =
    userAnswers.get(GamblingReturnPeriodsPage).flatMap(_.hasExistingNstpValues.map(!_))

  private def getNonStandardReturnPeriodsViewModel(userAnswers: UserAnswers)(implicit messages: Messages): Option[NonStandardReturnPeriodsViewModel] =
    for {
      gamblingReturnPeriods <- userAnswers.get(GamblingReturnPeriodsPage)
      viewModel             <- NonStandardReturnPeriodsViewModel.from(gamblingReturnPeriods)
    } yield viewModel
}
