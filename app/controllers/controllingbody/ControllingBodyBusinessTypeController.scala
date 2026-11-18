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
import forms.controllingbody.ControllingBodyBusinessTypeFormProvider
import models.{BusinessType, Mode, UserAnswers}
import navigation.Navigator
import pages.controllingbody.{ControllingBodyBusinessPartnerNumberPage, ControllingBodyBusinessTypePage, ControllingBodyChangesPage, ControllingBodyPreviousBusinessTypePage, ControllingBodySubmittedPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.controllingbody.ControllingBodyBusinessTypeView
import utils.FlagsUtil.checkIfChanged

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class ControllingBodyBusinessTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: ControllingBodyDetailsDataRequiredAction,
  formProvider: ControllingBodyBusinessTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ControllingBodyBusinessTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[BusinessType] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(ControllingBodyBusinessTypePage) match {
      case Some(businessType) => form.fill(businessType)
      case None               => form
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        businessType =>
          val ua = request.userAnswers
          val isChanged = checkIfChanged(businessType, ua, ControllingBodyBusinessTypePage, ControllingBodyChangesPage)
          for {
            withPreviousType            <- Future.fromTry(recordPreviousBusinessType(ua, businessType))
            updatedAnswers              <- Future.fromTry(withPreviousType.set(ControllingBodyBusinessTypePage, businessType))
            updatedAnswersWithSubmitted <- Future.fromTry(updatedAnswers.set(ControllingBodySubmittedPage, true))
            finalAnswers                <- Future.fromTry(updatedAnswersWithSubmitted.set(ControllingBodyChangesPage, isChanged))
            _                           <- sessionRepository.set(finalAnswers)
          } yield Redirect(navigator.nextPage(ControllingBodyBusinessTypePage, mode, updatedAnswers))
      )
  }

  // Keeps the type the existing controlling body had before the first change, so that the navigation can follow the
  // mini-flow for a change of type. A first-time setup has no business partner number and records nothing.
  private def recordPreviousBusinessType(ua: UserAnswers, businessType: BusinessType): Try[UserAnswers] =
    ua.get(ControllingBodyBusinessTypePage) match {
      case Some(current)
          if current != businessType &&
            ua.get(ControllingBodyPreviousBusinessTypePage).isEmpty &&
            ua.get(ControllingBodyBusinessPartnerNumberPage).exists(_.trim.nonEmpty) =>
        ua.set(ControllingBodyPreviousBusinessTypePage, current)
      case _ =>
        Success(ua)
    }
}
