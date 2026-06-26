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
import forms.AssociatedRegNumberFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import pages.{AssociatedRegNumberPage, AssociatedRegNumberSubmittedPage, AssociatedRegistrationNumbersPage, TradingDetailsChangesPage}
import play.api.data.Form
import utils.FlagsUtil.flagIfChanged
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AssociatedRegNumberView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AssociatedRegNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: MgdTradeDetailsDataRequiredAction,
  formProvider: AssociatedRegNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AssociatedRegNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()
  private val fieldName = "associatedRegNumber"

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(AssociatedRegNumberPage) match {
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
        associatedRegNumber => {
          val currentAssociatedRegNumbers = request.userAnswers.get(AssociatedRegistrationNumbersPage).getOrElse(Seq.empty)
          if (currentAssociatedRegNumbers.contains(associatedRegNumber)) {
            Future.successful(
              BadRequest(view(form.fill(associatedRegNumber).withError(fieldName, "associatedRegNumber.error.duplicate"), mode))
            )
          } else {
            val hasChanged = flagIfChanged(associatedRegNumber, sessionRepository, AssociatedRegNumberPage, TradingDetailsChangesPage)
            for {
              updatedAnswers <- Future.fromTry(updateUserAnswers(request.userAnswers, associatedRegNumber))
              updatedAnswers <- Future.fromTry(updatedAnswers.set(AssociatedRegNumberSubmittedPage, true))
              updatedAnswers <- Future.fromTry(updatedAnswers.set(TradingDetailsChangesPage, hasChanged))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(routes.AssociatedRegNumberController.onPageLoad(mode))
          }
        }
      )
  }

  private def updateUserAnswers(userAnswers: UserAnswers, associatedRegNumber: String): Try[UserAnswers] = {
    val currentAssociatedRegNumbers = userAnswers.get(AssociatedRegistrationNumbersPage).getOrElse(Seq.empty)
    val updatedAssociatedRegNumbers =
      if (currentAssociatedRegNumbers.contains(associatedRegNumber)) {
        currentAssociatedRegNumbers
      } else {
        currentAssociatedRegNumbers :+ associatedRegNumber
      }

    for {
      updatedAnswers <- userAnswers.set(AssociatedRegNumberPage, associatedRegNumber)
      updatedAnswers <- updatedAnswers.set(AssociatedRegistrationNumbersPage, updatedAssociatedRegNumbers)
    } yield updatedAnswers
  }
}
