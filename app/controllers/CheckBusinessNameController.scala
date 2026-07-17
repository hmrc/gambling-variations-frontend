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

import controllers.actions.{AuthorisedAction, BusinessNameDataRequiredAction, DataRetrievalAction}
import models.{BusinessType, SoleProprietorName}
import pages.*
import utils.FlagsUtil.checkFlag
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessNameView

import javax.inject.Inject

class CheckBusinessNameController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: BusinessNameDataRequiredAction,
  view: BusinessNameView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val result =
      request.userAnswers
        .get(GroupMemberPage)
        .map {
          case true =>
            Redirect(routes.AccessDeniedController.onPageLoad())

          case false =>
            val showChangeMessage: Boolean =
              checkFlag(request.userAnswers, BusinessNameChangesPage, BusinessNameSubmittedPage)

            val businessNameView: Option[Result] = for {
              businessName <- request.userAnswers.get(BusinessNamePage)
              businessType <- request.userAnswers.get(BusinessTypePage)
            } yield {
              Ok(view(businessType, businessName, request.userAnswers.get(TradingNamePage), showChangeMessage))
            }

            val soleProprietorView: Option[Result] =
              request.userAnswers.get(SoleProprietorPage).map { soleProprietor =>
                Ok(view(BusinessType.Soleproprietor, soleProprietor.fullName, request.userAnswers.get(TradingNamePage), showChangeMessage))
              }

            businessNameView
              .orElse(soleProprietorView)
              .getOrElse(Redirect(routes.SystemErrorController.onPageLoad()))
        }
        .getOrElse(Redirect(routes.SystemErrorController.onPageLoad()))

    result
  }
}
