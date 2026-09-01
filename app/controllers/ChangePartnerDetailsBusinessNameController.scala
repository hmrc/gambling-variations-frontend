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
import models.{BusinessType, Mode, UserAnswers}
import navigation.Navigator
import pages.*
import pages.partnerdetails.{PartnerDetailsBusinessNamePage, PartnerDetailsBusinessTypePage, PartnerDetailsSoleProprietorPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Reads.JsArrayReads
import play.api.libs.json.{JsArray, Reads}
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

  def onPageLoad(businessType: BusinessType, mode: Mode): Action[AnyContent] = {
    (authorise andThen getData andThen requireData) { implicit request =>
      val newIndex = getPartnersSize(request.userAnswers)

      (request.userAnswers.get(PartnerDetailsBusinessTypePage(newIndex)) map {
        case Soleproprietor =>
          val form = request.userAnswers
            .get(PartnerDetailsSoleProprietorPage(newIndex))
            .fold(soleProprietorFormProvider())(soleProp => soleProprietorFormProvider().fill(soleProp))
          Ok(changeSoleProprietorView(form, mode))
        case businessType =>
          val headingKey = BusinessTypeKeyBuilder.headingKeyFor(businessType)
          val titleKey = BusinessTypeKeyBuilder.titleKeyFor(businessType)
          val form = request.userAnswers
            .get(PartnerDetailsBusinessNamePage(newIndex))
            .fold(businessNameFormProvider(businessType))(businessName => businessNameFormProvider(businessType).fill(businessName))
          Ok(changeBusinessNameView(form, mode, businessType, headingKey, titleKey))
      }).getOrElse(Redirect(routes.SystemErrorController.onPageLoad()))
    }
  }

  def onSubmit(businessType: BusinessType, mode: Mode): Action[AnyContent] = {
    (authorise andThen getData andThen requireData).async { implicit request =>
      val newIndex = getPartnersSize(request.userAnswers)

      request.userAnswers.get(PartnerDetailsBusinessTypePage(newIndex)) map {
        case Soleproprietor =>
          soleProprietorFormProvider()
            .bindFromRequest()
            .fold(
              formWithErrors => {
                Future.successful(BadRequest(changeSoleProprietorView(formWithErrors, mode)))
              },
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerDetailsSoleProprietorPage(newIndex), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(PartnerDetailsSoleProprietorPage(newIndex), mode, updatedAnswers))
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
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerDetailsBusinessNamePage(newIndex), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(PartnerDetailsBusinessNamePage(newIndex), mode, updatedAnswers))
            )
      } getOrElse Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
    }
  }

  /*TODO: Important! This controller will be adding a new partner, it will have very minimal
     information at this stage and till the end before submitting this information it won't have businessPartnerNumber.
     Lack of it implies data is ONLY in the cache and has not been submitted yet.
   */
  private def getPartnersSize(userAnswers: UserAnswers) = (userAnswers.data \ "partners")
    .validate[JsArray]
    .asOpt
    .fold(0)(e => if e.value.isEmpty then 0 else e.value.size - 1)

}
