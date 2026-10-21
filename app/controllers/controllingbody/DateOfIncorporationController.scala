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
import forms.controllingbody.DateOfIncorporationFormProvider
import models.NormalMode
import navigation.Navigator
import pages.controllingbody.{ControllingBodyChangesPage, ControllingBodyDateOfIncorporationPage, ControllingBodySubmittedPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import utils.FlagsUtil.checkIfChanged
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.controllingbody.DateOfIncorporationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateOfIncorporationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: DateOfIncorporationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DateOfIncorporationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val form = formProvider()

    val preparedForm = request.userAnswers.get(ControllingBodyDateOfIncorporationPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    val form = formProvider()

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          val isChanged = checkIfChanged(value, request.userAnswers, ControllingBodyDateOfIncorporationPage, ControllingBodyChangesPage)

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ControllingBodyDateOfIncorporationPage, value))
            updatedAnswers <- Future.fromTry(updatedAnswers.set(ControllingBodySubmittedPage, true))
            updatedAnswers <- Future.fromTry(updatedAnswers.set(ControllingBodyChangesPage, isChanged))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ControllingBodyDateOfIncorporationPage, NormalMode, updatedAnswers))
      )
  }
}
