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
import forms.ContactNumberFormProvider
import models.Mode
import navigation.Navigator
import repositories.SessionRepository
import utils.FlagsUtil.flagIfChanged
import pages.{CorrespondenceContactNumberPage, CorrespondenceDetailsChangesPage, CorrespondenceDetailsSubmittedPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CorrespondenceContactNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CorrespondenceContactNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: CorrespondenceDetailsDataRequiredAction,
  formProvider: ContactNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CorrespondenceContactNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("correspondenceContactNumber")

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      val preparedForm = request.userAnswers
        .get(CorrespondenceContactNumberPage)
        .map(form.fill)
        .getOrElse(form)

      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      val boundForm = form.bindFromRequest()

      val validatedForm =
        if (
          boundForm("phoneNumber").value.forall(_.trim.isEmpty) &&
          boundForm("mobileNumber").value.forall(_.trim.isEmpty)
        ) {
          boundForm.withError(
            "phoneNumber",
            "correspondenceContactNumber.error.phoneNumber.required"
          )
        } else {
          boundForm
        }

      validatedForm.fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          val hasChanged: Future[Boolean] = flagIfChanged(value, sessionRepository, CorrespondenceContactNumberPage, CorrespondenceDetailsChangesPage)
          for {
            updatedAnswers <- Future.fromTry(
                                request.userAnswers.set(CorrespondenceContactNumberPage, value)
                              )
            updatedAnswers <- Future.fromTry(updatedAnswers.set(CorrespondenceDetailsSubmittedPage, true))
            changed        <- hasChanged
            updatedAnswers <- Future.fromTry(updatedAnswers.set(CorrespondenceDetailsChangesPage, changed))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(
            navigator.nextPage(CorrespondenceContactNumberPage, mode, updatedAnswers)
          )
      )
    }
}
