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
import forms.controllingbody.RemoveControllingBodyNINumberFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.RemoveControllingBodyNINumberPage
import pages.controllingbody.{ControllingBodyChangesPage, ControllingBodyNinoPage, ControllingBodySubmittedPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.controllingbody.RemoveControllingBodyNINumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class RemoveControllingBodyNINumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveControllingBodyNINumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveControllingBodyNINumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(RemoveControllingBodyNINumberPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    request.userAnswers.get(ControllingBodyNinoPage) match {
      case Some(nino) => Ok(view(preparedForm, nino))
      case _          => Redirect(controllers.routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val nino = request.userAnswers.get(ControllingBodyNinoPage).getOrElse("")
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, nino))),
        value =>
          for {
            updatedAnswers              <- Future.fromTry(updateUserAnswers(request.userAnswers, value))
            updatedAnswersWithSubmitted <- Future.fromTry(updatedAnswers.set(ControllingBodySubmittedPage, true))
            finalAnswer                 <- Future.fromTry(updatedAnswersWithSubmitted.set(ControllingBodyChangesPage, value))
            _                           <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RemoveControllingBodyNINumberPage, mode, updatedAnswers))
      )
  }

  private def updateUserAnswers(userAnswers: UserAnswers, value: Boolean): Try[UserAnswers] = {
    for {
      ua1 <- userAnswers.set(RemoveControllingBodyNINumberPage, value)
      ua2 <- {
        if (value) {
          ua1.set(ControllingBodyNinoPage, null) /*tombstone to track removal*/
        } else {
          Try(ua1)
        }
      }
    } yield ua2
  }

}
