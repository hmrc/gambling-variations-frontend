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
import forms.partnerdetails.PartnerDetailsCompanyRegistrationNumberFormProvider
import models.BusinessType.{Corporatebody, LimitedLiabilityPartnership}
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.BusinessNumberOrIndex
import pages.partnerdetails.{PartnerDetailsBusinessTypePage, PartnerDetailsCrnPage, PartnerDetailsIsBusinessIncorporatedUkPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PartnerUtils
import views.html.partnerdetails.PartnerDetailsCompanyRegistrationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerDetailsCompanyRegistrationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerDetailsCompanyRegistrationNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsCompanyRegistrationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(index: String, mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val newIndex = PartnerUtils.parseIndex(index, mode)

    if (crnApplies(request.userAnswers, newIndex)) {
      val preparedForm = request.userAnswers.get(PartnerDetailsCrnPage(newIndex)).fold(form)(form.fill)
      Ok(view(preparedForm, index, mode))
    } else {
      Redirect(controllers.routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(index: String, mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val newIndex = PartnerUtils.parseIndex(index, mode)

    if (!crnApplies(request.userAnswers, newIndex)) {
      Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
    } else {
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, index, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerDetailsCrnPage(newIndex), value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(PartnerDetailsCrnPage(newIndex), mode, updatedAnswers))
        )
    }
  }

  /** CRN applies to UK-incorporated corporate bodies and to LLPs. */
  private def crnApplies(answers: UserAnswers, index: BusinessNumberOrIndex): Boolean =
    (answers.get(PartnerDetailsBusinessTypePage(index)), answers.get(PartnerDetailsIsBusinessIncorporatedUkPage(index))) match {
      case (Some(Corporatebody), Some(true)) | (Some(LimitedLiabilityPartnership), _) => true
      case _                                                                          => false
    }
}
