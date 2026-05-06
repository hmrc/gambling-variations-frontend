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

import controllers.actions.{AuthorisedAction, DataRequiredAction, DataRetrievalAction}
import models.{BusinessNameDetails, BusinessType, SoleProprietorDetails}
import pages.{BusinessDetailsPage, SoleProprietorPage}
import models.BusinessType
import pages.{BusinessNameChangesPage, BusinessTypePage, TradingNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessNameView

import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Result
import javax.inject.Inject

import javax.inject.Inject

class CheckBusinessNameController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  authorised: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository,
  view: BusinessNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authorised andThen getData andThen requireData) { implicit request =>
    val businessNameDetails: Option[BusinessNameDetails] = request.userAnswers.get(BusinessDetailsPage)
    val soleProprietorName: Option[SoleProprietorDetails] = request.userAnswers.get(SoleProprietorPage)

    businessNameDetails map { business =>
      Ok(view(business.businessType.toString, business.businessName, business.tradingName))
    } orElse soleProprietorName.map { soleProprietor =>
      Ok(view(BusinessType.Soleproprietor.toString, soleProprietor.fullName, soleProprietor.tradingName))
    } getOrElse Redirect(routes.SystemErrorController.onPageLoad())
  }

  def onSubmit: Action[AnyContent] =
    (authorised andThen getData andThen requireData).async { implicit request =>

      val updatedAnswers = for {
        ua <- request.userAnswers.set(BusinessNameChangesPage, true)
      } yield ua

      updatedAnswers match {
        case scala.util.Success(answers) =>
          sessionRepository.set(answers).map { _ =>
            Redirect(routes.CheckBusinessNameController.onPageLoad()) // change to next page
          }

        case scala.util.Failure(_) =>
          Future.successful(InternalServerError("Unable to save data"))
      }
    }
}
