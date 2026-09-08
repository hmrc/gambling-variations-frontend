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

package controllers.licencespremises

import controllers.actions.*
import forms.licencespremises.RemovePremisesDetailsYesNoFormProvider
import models.Mode
import navigation.Navigator
import pages.licencespremises.{LicencesPremisesDetailsChangesPage, LicencesPremisesDetailsSubmittedPage, PremisesDetailsPage, RemovePremisesDetailsYesNoPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.licencespremises.RemovePremisesDetailsYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemovePremisesDetailsYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: LicencesPremisesDataRequiredAction,
  formProvider: RemovePremisesDetailsYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemovePremisesDetailsYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(RemovePremisesDetailsYesNoPage) match {
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
        value =>
          for {
            updatedAnswers <- if (value) {
                                Future.fromTry(request.userAnswers.remove(PremisesDetailsPage))
                              } else {
                                Future.successful(request.userAnswers)
                              }
            updatedAnswers <- Future.fromTry(updatedAnswers.set(RemovePremisesDetailsYesNoPage, value))
            updatedAnswers <- Future.fromTry(updatedAnswers.set(LicencesPremisesDetailsSubmittedPage, true))
            updatedAnswers <- Future.fromTry(updatedAnswers.set(LicencesPremisesDetailsChangesPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RemovePremisesDetailsYesNoPage, mode, updatedAnswers))
      )
  }
}
