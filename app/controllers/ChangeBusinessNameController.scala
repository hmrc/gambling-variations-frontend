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
import forms.{ChangeBusinessNameFormProvider, SoleProprietorNameFormProvider}
import models.BusinessType.Soleproprietor
import models.{BusinessType, Mode}
import navigation.Navigator
import pages.{BusinessNamePage, BusinessTypePage, SoleProprietorPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{ChangeBusinessNameView, SoleProprietorNameView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeBusinessNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ChangeBusinessNameFormProvider,
  soleProprietorFormProvider: SoleProprietorNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ChangeBusinessNameView,
  soleproprietorView: SoleProprietorNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def headingKeyFor(businessType: BusinessType): String =
    s"changeBusinessName.heading.${businessType.toString}"

  private def titleKeyFor(businessType: BusinessType): String =
    s"changeBusinessName.title.${businessType.toString}"

  def onPageLoad(mode: Mode, businessType: BusinessType): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      (
        request.userAnswers.get(BusinessTypePage) flatMap {
          case Soleproprietor =>
            request.userAnswers.get(SoleProprietorPage) map { soleProprietorName =>
              val form = soleProprietorFormProvider()
              val preparedForm = request.userAnswers.get(SoleProprietorPage).fold(form)(form.fill)
              Ok(soleproprietorView(preparedForm, mode))
            }
          case businessType =>
            request.userAnswers.get(BusinessNamePage) map { businessName =>
              val form = formProvider(businessType)
              val preparedForm = form.fill(businessName)
              val headingKey = headingKeyFor(businessType)
              val titleKey = titleKeyFor(businessType)
              Ok(view(preparedForm, mode, businessType, headingKey, titleKey))
            }
        }
      ) getOrElse Redirect(routes.CheckBusinessNameController.onPageLoad())
    }

  def onSubmit(mode: Mode, businessType: BusinessType): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(BusinessTypePage) map {
        case Soleproprietor =>
          soleProprietorFormProvider()
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(soleproprietorView(formWithErrors, mode))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(SoleProprietorPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(BusinessNamePage, mode, updatedAnswers))
            )
        case businessType => {
          val headingKey = headingKeyFor(businessType)
          val titleKey = titleKeyFor(businessType)
          formProvider(businessType)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, businessType, headingKey, titleKey))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessNamePage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(BusinessNamePage, mode, updatedAnswers))
            )
        }
      } getOrElse Future.successful(Redirect(routes.CheckBusinessNameController.onPageLoad()))
    }

}
