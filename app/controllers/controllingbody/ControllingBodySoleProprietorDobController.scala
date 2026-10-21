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
import forms.controllingbody.ControllingBodySoleProprietorDobFormProvider
import models.{BusinessType, Mode, UserAnswers}
import navigation.Navigator
import pages.controllingbody.{ControllingBodyBusinessTypePage, ControllingBodyChangesPage, ControllingBodyDateOfBirthPage, ControllingBodySubmittedPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FlagsUtil.checkIfChanged
import views.html.controllingbody.ControllingBodySoleProprietorDobView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ControllingBodySoleProprietorDobController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: ControllingBodyDetailsDataRequiredAction,
  formProvider: ControllingBodySoleProprietorDobFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ControllingBodySoleProprietorDobView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      if (!shouldShowPartnerSoleProprietorDob(request.userAnswers)) {
        Redirect(controllers.routes.SystemErrorController.onPageLoad())
      } else {
        val form = formProvider()

        val preparedForm =
          request.userAnswers
            .get(ControllingBodyDateOfBirthPage)
            .fold(form)(form.fill)

        Ok(view(preparedForm, mode))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val form = formProvider()

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        dob =>
          val ua = request.userAnswers
          val isChanged = checkIfChanged(dob, ua, ControllingBodyDateOfBirthPage, ControllingBodyChangesPage)
          for {
            updatedAnswers              <- Future.fromTry(request.userAnswers.set(ControllingBodyDateOfBirthPage, dob))
            updatedAnswersWithSubmitted <- Future.fromTry(updatedAnswers.set(ControllingBodySubmittedPage, true))
            finalAnswers                <- Future.fromTry(updatedAnswersWithSubmitted.set(ControllingBodyChangesPage, isChanged))
            _                           <- sessionRepository.set(finalAnswers)
          } yield Redirect(navigator.nextPage(ControllingBodyDateOfBirthPage, mode, updatedAnswers))
      )
  }

  private def shouldShowPartnerSoleProprietorDob(userAnswers: UserAnswers): Boolean =
    userAnswers.get(ControllingBodyBusinessTypePage).contains(BusinessType.Soleproprietor)
}
