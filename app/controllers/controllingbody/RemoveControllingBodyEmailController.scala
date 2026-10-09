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
import forms.controllingbody.RemoveControllingBodyEmailFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.controllingbody.{ControllingBodyChangesPage, ControllingBodyEmailPage, ControllingBodySubmittedPage, RemoveControllingBodyEmailPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.controllingbody.RemoveControllingBodyEmailView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class RemoveControllingBodyEmailController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: ControllingBodyDetailsDataRequiredAction,
  formProvider: RemoveControllingBodyEmailFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveControllingBodyEmailView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    request.userAnswers.get(ControllingBodyEmailPage) match {
      case Some(email) =>
        Ok(view(form, mode, email))
      case None =>
        Redirect(routes.SystemErrorController.onPageLoad())
    }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) async { implicit request =>

    request.userAnswers
      .get(ControllingBodyEmailPage)
      .map { email =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, email))),
            value =>
              for {
                updatedAnswers              <- Future.fromTry(updateUserAnswers(request.userAnswers, value))
                updatedAnswersWithSubmitted <- Future.fromTry(updatedAnswers.set(ControllingBodySubmittedPage, true))
                finalAnswer                 <- Future.fromTry(updatedAnswersWithSubmitted.set(ControllingBodyChangesPage, value))
                _                           <- sessionRepository.set(finalAnswer)
              } yield Redirect(navigator.nextPage(RemoveControllingBodyEmailPage, mode, finalAnswer))
          )
          .recover { case ex =>
            Redirect(routes.SystemErrorController.onPageLoad())
          }
      }
      .getOrElse(Future.successful(Redirect(routes.SystemErrorController.onPageLoad())))
  }

  private def updateUserAnswers(userAnswers: UserAnswers, value: Boolean): Try[UserAnswers] = {
    for {
      ua1 <- userAnswers.set(RemoveControllingBodyEmailPage, value)
      ua2 <- {
        if (value) {
          ua1.remove(ControllingBodyEmailPage)
        } else {
          Try(ua1)
        }
      }
    } yield ua2
  }

}
