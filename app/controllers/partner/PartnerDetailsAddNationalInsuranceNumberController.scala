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
import utils.PartnerUtils.getPartnersSize
import forms.partner.PartnerDetailsAddNationalInsuranceNumberFormProvider
import models.Mode
import navigation.Navigator
import pages.partnerdetails.PartnerDetailsNinoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.partner.PartnerDetailsAddNationalInsuranceNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerDetailsAddNationalInsuranceNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerDetailsAddNationalInsuranceNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsAddNationalInsuranceNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  /*TODO: Important! This controller will be adding a new partner, it will have very minimal
     information at this stage and till the end before submitting this information it won't have businessPartnerNumber.
     Lack of it implies data is ONLY in the cache and has not been submitted yet.
   */

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val index: Int = request.userAnswers.getPartnersSize

    val preparedForm = request.userAnswers.get(PartnerDetailsNinoPage(index)) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val index: Int = request.userAnswers.getPartnersSize

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        nino =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerDetailsNinoPage(index), nino))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PartnerDetailsNinoPage(index), mode, updatedAnswers))
      )
  }
}
