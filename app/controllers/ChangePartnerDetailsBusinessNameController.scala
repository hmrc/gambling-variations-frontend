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
import pages.*
import pages.partnerdetails.addpartnerdetails.{AddPartnerDetailsBusinessNamePage, AddPartnerDetailsBusinessTypePage, AddPartnerDetailsSoleProprietorPage}
import pages.partnerdetails.{PartnerDetailsBusinessNamePage, PartnerDetailsBusinessTypePage, PartnerDetailsSoleProprietorPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.BusinessTypeKeyBuilder
import views.html.{ChangePartnerDetailsBusinessNameView, ChangePartnerDetailsSoleProprietorNameView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangePartnerDetailsBusinessNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  businessNameFormProvider: ChangeBusinessNameFormProvider,
  soleProprietorFormProvider: SoleProprietorNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  changeBusinessNameView: ChangePartnerDetailsBusinessNameView,
  changeSoleProprietorView: ChangePartnerDetailsSoleProprietorNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(businessType: BusinessType, mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(AddPartnerDetailsBusinessTypePage) match {
      case Some(businessType @ BusinessType.Soleproprietor) =>
        val newForm = request.userAnswers
          .get(AddPartnerDetailsSoleProprietorPage)
          .fold(soleProprietorFormProvider())(soleProp => soleProprietorFormProvider().fill(soleProp))
        Ok(changeSoleProprietorView(newForm, mode))
      case Some(businessType) =>
        val newForm = request.userAnswers
          .get(AddPartnerDetailsBusinessNamePage)
          .fold(businessNameFormProvider(businessType))(businessName => businessNameFormProvider(businessType).fill(businessName))
        val headingKey = BusinessTypeKeyBuilder.headingKeyFor(businessType)
        val titleKey = BusinessTypeKeyBuilder.titleKeyFor(businessType)

        Ok(changeBusinessNameView(newForm, mode, businessType, headingKey, titleKey))
      case _ =>
        Redirect(routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(businessType: BusinessType, mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(AddPartnerDetailsBusinessTypePage) match {

        case Some(businessType @ BusinessType.Soleproprietor) =>
          soleProprietorFormProvider()
            .bindFromRequest()
            .fold(
              formWithErrors => {
                Future.successful(BadRequest(changeSoleProprietorView(formWithErrors, mode)))
              },
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(AddPartnerDetailsSoleProprietorPage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AddPartnerDetailsSoleProprietorPage, mode, updatedAnswers))
            )
        case Some(businessType) =>
          val headingKey = BusinessTypeKeyBuilder.headingKeyFor(businessType)
          val titleKey = BusinessTypeKeyBuilder.titleKeyFor(businessType)

          businessNameFormProvider(businessType)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(changeBusinessNameView(formWithErrors, mode, businessType, headingKey, titleKey))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(AddPartnerDetailsBusinessNamePage, value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(AddPartnerDetailsBusinessNamePage, mode, updatedAnswers))
            )
        case _ =>
          Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
      }
  }

}
