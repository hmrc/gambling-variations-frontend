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

package controllers.partner

import controllers.actions.*
import controllers.partner.PartnerUtils.getIndex
import forms.partner.PartnerSoleProprietorDobFormProvider
import models.{BusinessType, Mode, UserAnswers}
import navigation.Navigator
import pages.partnerdetails.{PartnerDetailsBusinessTypePage, PartnerDetailsDateOfBirthPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.partner.PartnerSoleProprietorDobView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerSoleProprietorDobController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerSoleProprietorDobFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerSoleProprietorDobView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      val index: Int = request.userAnswers.getIndex
      if (!shouldShowPartnerSoleProprietorDob(request.userAnswers, index)) {
        Redirect(controllers.routes.SystemErrorController.onPageLoad())
      } else {
        val form = formProvider()

        val preparedForm =
          request.userAnswers
            .get(PartnerDetailsDateOfBirthPage(index))
            .fold(form)(form.fill)

        Ok(view(preparedForm, mode))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    val index: Int = request.userAnswers.getIndex
    val form = formProvider()

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerDetailsDateOfBirthPage(index), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PartnerDetailsDateOfBirthPage(index), mode, updatedAnswers))
      )
  }

  private def shouldShowPartnerSoleProprietorDob(userAnswers: UserAnswers, index: Int): Boolean =
    userAnswers.get(PartnerDetailsBusinessTypePage(index)).contains(BusinessType.Soleproprietor)
}
