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

package controllers

import controllers.actions.*

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import forms.PreviousRegistrationNumbersFormProvider
import pages.{AddPreviousRegistrationNumberPage, ChosenPreviousRegNumberPage, PreviousRegistrationNumbersPage, UnsubmittedPreviousRegistrationNumbersPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PreviousRegistrationNumbersView

import scala.concurrent.{ExecutionContext, Future}

class PreviousRegistrationNumbersController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: MgdTradeDetailsDataRequiredAction,
  formProvider: PreviousRegistrationNumbersFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PreviousRegistrationNumbersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(AddPreviousRegistrationNumberPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }
    val previousRegNumberSeq: Option[Seq[String]] = request.userAnswers.get(PreviousRegistrationNumbersPage)

    val unsubmittedPreviousRegNumberSeq: Option[Seq[String]] = request.userAnswers.get(UnsubmittedPreviousRegistrationNumbersPage)

    val submittedRegNumbersCount: Int = previousRegNumberSeq match {
      case Some(sequence) => sequence.length
      case None           => 0
    }

    val unsubmittedRegNumbersCount: Int = unsubmittedPreviousRegNumberSeq match {
      case Some(sequence) => sequence.length
      case None           => 0
    }

    Ok(view(preparedForm, mode, previousRegNumberSeq, unsubmittedPreviousRegNumberSeq, submittedRegNumbersCount, unsubmittedRegNumbersCount))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val previousRegNumberSeq: Option[Seq[String]] = request.userAnswers.get(PreviousRegistrationNumbersPage)
    val unsubmittedPreviousRegNumberSeq: Option[Seq[String]] = request.userAnswers.get(UnsubmittedPreviousRegistrationNumbersPage)
    val submittedRegNumbersCount: Int = previousRegNumberSeq match {
      case Some(sequence) => sequence.length
      case None           => 0
    }

    val unsubmittedRegNumbersCount: Int = unsubmittedPreviousRegNumberSeq match {
      case Some(sequence) => sequence.length
      case None           => 0
    }

    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          if (submittedRegNumbersCount + unsubmittedRegNumbersCount < 3) {
            Future
              .successful(
                BadRequest(
                  view(formWithErrors,
                       mode,
                       previousRegNumberSeq,
                       unsubmittedPreviousRegNumberSeq,
                       submittedRegNumbersCount,
                       unsubmittedRegNumbersCount
                      )
                )
              )
          } else {
            Future.successful(Redirect("#"))
          },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddPreviousRegistrationNumberPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AddPreviousRegistrationNumberPage, mode, updatedAnswers))
      )
  }

  def onRedirect(mode: Mode, prevRegNumber: String): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(ChosenPreviousRegNumberPage, prevRegNumber))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(routes.RemovePreviousRegNumberController.onPageLoad(mode))
    }

}
