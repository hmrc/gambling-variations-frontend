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

import connectors.GamblingConnector
import controllers.actions.*

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.tradingdetails.*
import views.html.CheckTradingDetailsView
import pages.{GroupMemberPage, TradingDetailsChangeFlagPage}

import scala.concurrent.{ExecutionContext, Future}

class CheckTradingDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  authorised: AuthorisedAction,
  getData: DataRetrievalAction,
  checkTradingDetailsDataRequired: MgdTradeDetailsDataRequiredAction,
  gamblingConnector: GamblingConnector,
  val controllerComponents: MessagesControllerComponents,
  view: CheckTradingDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (authorised andThen getData andThen checkTradingDetailsDataRequired).async { implicit request =>

      val showChangeMessage: Boolean =
        request.userAnswers
          .get(TradingDetailsChangeFlagPage)
          .contains(true)

      val isGroupMemberF: Future[Boolean] =
        request.userAnswers.get(GroupMemberPage) match {

          case Some(value) =>
            Future.successful(value)

          case None =>
            gamblingConnector
              .getBusinessDetails(request.mgdRegNum)
              .map(_.groupReg)
        }

      isGroupMemberF.map { isGroupMember =>

        val vm =
          CheckTradingDetailsViewModel.from(
            request.userAnswers,
            isGroupMember
          )

        Ok(
          view(
            vm.list,
            vm.previousMgd,
            vm.associatedMgd,
            showChangeMessage
          )
        )
      }
    }
}
