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

package controllers.controllingbody

import controllers.actions.*
import controllers.routes
import forms.controllingbody.RemoveControllingBodyFaxNumberFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.controllingbody.{ControllingBodyChangesPage, ControllingBodyFaxNumberPage, ControllingBodySubmittedPage, RemoveControllingBodyFaxNumberPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.controllingbody.RemoveControllingBodyFaxNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class RemoveControllingBodyFaxNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: ControllingBodyDetailsDataRequiredAction,
  formProvider: RemoveControllingBodyFaxNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveControllingBodyFaxNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(ControllingBodyFaxNumberPage) match {
      case Some(faxNumber) => Ok(view(form, mode, faxNumber))
      case None            => Redirect(routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(ControllingBodyFaxNumberPage) match {
      case Some(faxNumber) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, faxNumber))),
            remove =>
              for {
                updatedAnswers <- Future.fromTry(updateUserAnswers(request.userAnswers, remove))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(RemoveControllingBodyFaxNumberPage, mode, updatedAnswers))
          )
      case None =>
        Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
    }
  }

  // Answering no must not discard a change already made to the section on an earlier screen
  private def updateUserAnswers(userAnswers: UserAnswers, remove: Boolean): Try[UserAnswers] = {
    val isChanged = remove || userAnswers.get(ControllingBodyChangesPage).contains(true)

    for {
      updatedAnswers <- userAnswers.set(RemoveControllingBodyFaxNumberPage, remove)
      updatedAnswers <- if (remove) updatedAnswers.remove(ControllingBodyFaxNumberPage) else Try(updatedAnswers)
      updatedAnswers <- updatedAnswers.set(ControllingBodySubmittedPage, true)
      updatedAnswers <- updatedAnswers.set(ControllingBodyChangesPage, isChanged)
    } yield updatedAnswers
  }
}
