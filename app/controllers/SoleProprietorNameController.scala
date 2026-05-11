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
import forms.SoleProprietorNameFormProvider
import models.{Mode, SoleProprietorName, UserAnswers}
import navigation.Navigator
import pages.SoleProprietorNamePage
import play.api.data.Form
import play.api.i18n.Lang.logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SoleProprietorNameFormView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SoleProprietorNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: SoleProprietorNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SoleProprietorNameFormView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData).async { implicit request =>

    val form: Form[SoleProprietorName] = formProvider()

    logger.info("Reached SoleProprietorNameController")

    val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.mgdRegNum))

    val preparedForm = userAnswers
      .get(SoleProprietorNamePage)
      .fold(form)(form.fill)

    // This prevents requireData from calling backend next time
    val initialiseUserAnswers: Future[Boolean] =
      if (request.userAnswers.isEmpty)
        sessionRepository.set(userAnswers)
      else Future.successful(true)

    initialiseUserAnswers.map { _ =>
      Ok(view(preparedForm, mode))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    val form = formProvider()

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SoleProprietorNamePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(routes.SoleProprietorNameController.onPageLoad(mode))
        // yield Redirect(navigator.nextPage(SoleProprietorNamePage, mode, updatedAnswers))
      )
  }
}
