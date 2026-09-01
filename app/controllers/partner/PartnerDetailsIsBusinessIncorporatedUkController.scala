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
import forms.PartnerDetailsIsBusinessIncorporatedUkFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.partnerdetails.PartnerDetailsIsBusinessIncorporatedUkPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsArray
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.partner.PartnerDetailsIsBusinessIncorporatedUkView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerDetailsIsBusinessIncorporatedUkController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerDetailsIsBusinessIncorporatedUkFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsIsBusinessIncorporatedUkView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val newIndex = getPartnersSize(request.userAnswers)

    val preparedForm = request.userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(newIndex)) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val newIndex = getPartnersSize(request.userAnswers)

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerDetailsIsBusinessIncorporatedUkPage(newIndex), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PartnerDetailsIsBusinessIncorporatedUkPage(newIndex), mode, updatedAnswers))
      )
  }

  // TODO delete, luca made extension object
  private def getPartnersSize(userAnswers: UserAnswers): Int = (userAnswers.data \ "partners")
    .validate[JsArray]
    .asOpt
    .fold(0)(e => if e.value.isEmpty then 0 else e.value.size - 1)
}
