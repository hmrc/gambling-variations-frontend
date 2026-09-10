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
import controllers.routes
import forms.partner.PartnerDetailsRemoveNationalInsuranceNumberYesNoFormProvider
import models.Mode
import navigation.Navigator
import pages.partner.PartnerDetailsRemoveNationalInsuranceNumberYesNoPage
import pages.partnerdetails.PartnerDetailsNinoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.partner.PartnerDetailsRemoveNationalInsuranceNumberYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerDetailsRemoveNationalInsuranceNumberYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerDetailsRemoveNationalInsuranceNumberYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsRemoveNationalInsuranceNumberYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val index: Int = request.userAnswers.getIndex
    val preparedForm = request.userAnswers.get(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(index)) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    request.userAnswers
      .get(PartnerDetailsNinoPage(index)) match {
      case Some(nino) =>
        Ok(view(preparedForm, mode, nino))

      case None =>
        Redirect(routes.JourneyRecoveryController.onPageLoad())
    }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val index: Int = request.userAnswers.getIndex
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              view(formWithErrors,
                   mode,
                   request.userAnswers
                     .get(PartnerDetailsNinoPage(index))
                     .getOrElse("")
                  )
            )
          ),
        value =>
          for {
            updatedAnswers <- if (value) {
                                Future.fromTry(request.userAnswers.remove(PartnerDetailsNinoPage(index)))
                              } else {
                                Future.apply(request.userAnswers)
                              }
            updatedAnswers <- Future.fromTry(updatedAnswers.set(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(index), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PartnerDetailsNinoPage(index), mode, updatedAnswers))
      )
  }
}
