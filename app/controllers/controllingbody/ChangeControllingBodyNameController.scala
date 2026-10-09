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
import controllers.routes
import forms.{ChangeBusinessNameFormProvider, SoleProprietorNameFormProvider}
import models.BusinessType.Soleproprietor
import models.{BusinessType, Mode}
import navigation.Navigator
import pages.controllingbody.{ControllingBodyBusinessNamePage, ControllingBodyBusinessTypePage, ControllingBodySoleProprietorPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.controllingbody.{ChangeControllingBodyNameView, ChangeControllingBodySoleProprietorNameView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeControllingBodyNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: ControllingBodyDetailsDataRequiredAction,
  businessNameFormProvider: ChangeBusinessNameFormProvider,
  soleProprietorFormProvider: SoleProprietorNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  businessNameView: ChangeControllingBodyNameView,
  soleProprietorView: ChangeControllingBodySoleProprietorNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(businessType: BusinessType, mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      request.userAnswers.get(ControllingBodyBusinessTypePage).filter(_ == businessType) match {
        case Some(Soleproprietor) =>
          val form = soleProprietorFormProvider()
          val preparedForm = request.userAnswers.get(ControllingBodySoleProprietorPage).fold(form)(form.fill)
          Ok(soleProprietorView(preparedForm))

        case Some(bodyType) =>
          val form = businessNameFormProvider(bodyType)
          val preparedForm = request.userAnswers.get(ControllingBodyBusinessNamePage).fold(form)(form.fill)
          Ok(businessNameView(preparedForm, bodyType))

        case None =>
          Redirect(routes.SystemErrorController.onPageLoad())
      }
    }

  def onSubmit(businessType: BusinessType, mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(ControllingBodyBusinessTypePage).filter(_ == businessType) match {
        case Some(Soleproprietor) =>
          soleProprietorFormProvider()
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(soleProprietorView(formWithErrors))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(ControllingBodySoleProprietorPage, value))
                  saved          <- sessionRepository.set(updatedAnswers)
                } yield {
                  saved match {
                    case true  => Redirect(navigator.nextPage(ControllingBodySoleProprietorPage, mode, updatedAnswers))
                    case false => Redirect(routes.SystemErrorController.onPageLoad())
                  }
                }
            )

        case Some(bodyType) =>
          businessNameFormProvider(bodyType)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(businessNameView(formWithErrors, bodyType))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(ControllingBodyBusinessNamePage, value))
                  saved          <- sessionRepository.set(updatedAnswers)
                } yield {
                  saved match {
                    case true  => Redirect(navigator.nextPage(ControllingBodyBusinessNamePage, mode, updatedAnswers))
                    case false => Redirect(routes.SystemErrorController.onPageLoad())
                  }
                }
            )

        case None =>
          Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
      }
    }
}
