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
import forms.{ChangeBusinessNameFormProvider, SoleProprietorNameFormProvider}
import models.BusinessType.Soleproprietor
import models.{BusinessType, Mode}
import navigation.Navigator
import pages.*
import pages.partnerdetails.{PartnerDetailsBusinessNamePage, PartnerDetailsBusinessTypePage, PartnerDetailsSoleProprietorPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Reads
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
  requireData: PartnerDetailsDataRequiredAction,
  businessNameFormProvider: ChangeBusinessNameFormProvider,
  soleProprietorFormProvider: SoleProprietorNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  changeBusinessNameView: ChangePartnerDetailsBusinessNameView,
  changeSoleProprietorView: ChangePartnerDetailsSoleProprietorNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  /*TODO: Important! This controller will be adding a new partner, it will have very minimal
     information at this stage and till the end before submitting this information it won't have businessPartnerNumber.
     Lack of it implies data is ONLY in the cache and has not been submitted yet.
   */

  def onPageLoad(businessType: BusinessType, mode: Mode): Action[AnyContent] = {
    (authorise andThen getData andThen requireData) { implicit request =>
      val index: Int = request.userAnswers.getIndex

      (request.userAnswers.get(PartnerDetailsBusinessTypePage(index)) map {
        case Soleproprietor =>
          val form = request.userAnswers
            .get(PartnerDetailsSoleProprietorPage(index))
            .fold(soleProprietorFormProvider())(soleProp => soleProprietorFormProvider().fill(soleProp))
          Ok(changeSoleProprietorView(form, mode))
        case businessType =>
          val headingKey = BusinessTypeKeyBuilder.headingKeyFor(businessType)
          val titleKey = BusinessTypeKeyBuilder.titleKeyFor(businessType)
          val form = request.userAnswers
            .get(PartnerDetailsBusinessNamePage(index))
            .fold(businessNameFormProvider(businessType))(businessName => businessNameFormProvider(businessType).fill(businessName))
          Ok(changeBusinessNameView(form, mode, businessType, headingKey, titleKey))
      }).getOrElse(Redirect(routes.SystemErrorController.onPageLoad()))
    }
  }

  def onSubmit(businessType: BusinessType, mode: Mode): Action[AnyContent] = {
    (authorise andThen getData andThen requireData).async { implicit request =>
      val index: Int = request.userAnswers.getIndex

      request.userAnswers.get(PartnerDetailsBusinessTypePage(index)) map {
        case Soleproprietor =>
          soleProprietorFormProvider()
            .bindFromRequest()
            .fold(
              formWithErrors => {
                Future.successful(BadRequest(changeSoleProprietorView(formWithErrors, mode)))
              },
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerDetailsSoleProprietorPage(index), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(PartnerDetailsSoleProprietorPage(index), mode, updatedAnswers))
            )
        case businessType =>
          val headingKey = BusinessTypeKeyBuilder.headingKeyFor(businessType)
          val titleKey = BusinessTypeKeyBuilder.titleKeyFor(businessType)

          businessNameFormProvider(businessType)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(changeBusinessNameView(formWithErrors, mode, businessType, headingKey, titleKey))),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerDetailsBusinessNamePage(index), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(PartnerDetailsBusinessNamePage(index), mode, updatedAnswers))
            )
      } getOrElse Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
    }
  }

}
