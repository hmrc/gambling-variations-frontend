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
import forms.PreviousRegistrationNumberFormProvider
import utils.FlagsUtil.flagIfChanged

import javax.inject.Inject
import models.{Mode, UserAnswers}
import pages.{PreviousRegNumberPage, PreviousRegistrationNumbersPage, TradingDetailsChangesPage, UnsubmittedPreviousRegNumbersPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PreviousRegistrationNumberView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class PreviousRegistrationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: MgdTradeDetailsDataRequiredAction,
  formProvider: PreviousRegistrationNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PreviousRegistrationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()
  private val fieldName = "previousRegistrationNumber"

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(PreviousRegNumberPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        previousRegistrationNumber => {
          val hasChanged: Future[Boolean] =
            flagIfChanged(previousRegistrationNumber, sessionRepository, PreviousRegistrationNumbersPage, TradingDetailsChangesPage)
          val currentPreviousRegistrationNumbers = request.userAnswers.get(PreviousRegistrationNumbersPage).getOrElse(Seq.empty)

          if (currentPreviousRegistrationNumbers.contains(previousRegistrationNumber)) {
            Future.successful(
              BadRequest(view(form.fill(previousRegistrationNumber).withError(fieldName, "previousRegistrationNumber.error.duplicate"), mode))
            )
          } else {
            for {
              updatedAnswers <- Future.fromTry(updateUserAnswers(request.userAnswers, previousRegistrationNumber))
              changed        <- hasChanged
              updatedAnswers <- Future.fromTry(updatedAnswers.set(TradingDetailsChangesPage, changed))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(routes.PreviousRegistrationNumberController.onPageLoad(mode))
          }
        }
      )
  }

  private def updateUserAnswers(userAnswers: UserAnswers, previousRegistrationNumber: String): Try[UserAnswers] = {
    val currentPreviousRegistrationNumbers = userAnswers.get(UnsubmittedPreviousRegNumbersPage).getOrElse(Seq.empty)
    val updatedPreviousRegistrationNumbers =
      if (currentPreviousRegistrationNumbers.contains(previousRegistrationNumber)) {
        currentPreviousRegistrationNumbers
      } else {
        currentPreviousRegistrationNumbers :+ previousRegistrationNumber
      }

    for {
      updatedAnswers <- userAnswers.set(PreviousRegNumberPage, previousRegistrationNumber)
      updatedAnswers <- updatedAnswers.set(PreviousRegistrationNumbersPage, updatedPreviousRegistrationNumbers)
    } yield updatedAnswers
  }
}
