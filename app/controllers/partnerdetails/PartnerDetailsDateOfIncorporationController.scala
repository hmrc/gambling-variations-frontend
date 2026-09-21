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

package controllers.partnerdetails

import controllers.actions.*
import forms.partnerdetails.PartnerDateOfIncorporationFormProvider
import models.{BusinessType, Mode, UserAnswers}
import navigation.Navigator
import pages.BusinessNumberOrIndex
import pages.partnerdetails.{PartnerDetailsBusinessTypePage, PartnerDetailsDateOfIncorporation, PartnerDetailsIsBusinessIncorporatedUkPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PartnerUtils
import views.html.partnerdetails.PartnerDetailsDateOfIncorporationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

//TODO this controller has changed, note the route for QA
class PartnerDetailsDateOfIncorporationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerDateOfIncorporationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsDateOfIncorporationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(index: String, mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      val newIndex = PartnerUtils.parseIndex(index, mode)
      if (!shouldShowDateOfIncorporation(request.userAnswers, newIndex)) {
        Redirect(controllers.routes.SystemErrorController.onPageLoad())
      } else {
        val form = formProvider()

        val preparedForm =
          request.userAnswers
            .get(PartnerDetailsDateOfIncorporation(newIndex))
            .fold(form)(form.fill)

        Ok(view(preparedForm, index, mode))
      }
    }

  def onSubmit(index: String, mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val newIndex = PartnerUtils.parseIndex(index, mode)
    val form = formProvider()

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, index, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerDetailsDateOfIncorporation(newIndex), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PartnerDetailsDateOfIncorporation(newIndex), mode, updatedAnswers))
      )
  }

  private def shouldShowDateOfIncorporation(userAnswers: UserAnswers, index: BusinessNumberOrIndex): Boolean =
    userAnswers.get(PartnerDetailsBusinessTypePage(index)) match {
      case Some(BusinessType.Corporatebody) =>
        userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(index)).contains(true)
      case Some(BusinessType.LimitedLiabilityPartnership) =>
        true
      case _ =>
        false
    }
}
