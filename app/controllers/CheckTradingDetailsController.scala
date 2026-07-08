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
import utils.FlagsUtil.checkFlag

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.tradingdetails.*
import views.html.CheckTradingDetailsView
import pages.*
import models.*

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

      val showChangeMessage: Boolean = checkFlag(request.userAnswers, TradingDetailsChangesPage, TradingDetailsChangeFlagPage)

      val isGroupMemberF: Future[Boolean] =
        request.userAnswers.get(GroupMemberPage) match {
          case Some(value) => Future.successful(value)
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

  def onPreviousRegNumbers: Action[AnyContent] =
    (authorised andThen getData andThen checkTradingDetailsDataRequired) { implicit request =>

      val previousRegsExist =
        CheckTradingDetailsViewModel.from(request.userAnswers, isGroupMember = false).previousMgd.rows.nonEmpty

      if (previousRegsExist) {
        Redirect(routes.PreviousRegistrationNumbersListController.onPageLoad(NormalMode))
      } else {
        Redirect(routes.PreviousRegistrationNumberController.onPageLoad(NormalMode))
      }
    }

  def onAssociatedRegNumbers: Action[AnyContent] =
    (authorised andThen getData andThen checkTradingDetailsDataRequired) { implicit request =>

      val associatedRegsExist =
        CheckTradingDetailsViewModel.from(request.userAnswers, isGroupMember = false).associatedMgd.rows.nonEmpty

      if (associatedRegsExist) {
        Redirect(routes.AssociatedRegistrationNumbersListController.onPageLoad(NormalMode))
      } else {
        Redirect(routes.AssociatedRegNumberController.onPageLoad(NormalMode))
      }
    }

  def onContinue: Action[AnyContent] =
    (authorised andThen getData andThen checkTradingDetailsDataRequired) { implicit request =>

      val tradeClassOpt = request.userAnswers.get(BusinessTradeClassPage)
      val seasonalOpt = request.userAnswers.get(IsSeasonalBusinessPage)
      val otherDescOpt = request.userAnswers.get(OtherTradeClassPage)

      def stringMissing(opt: Option[String]): Boolean =
        opt.forall(s => s.trim.isEmpty || s.trim.equalsIgnoreCase("Not provided"))

      def tradeClassIsMissing: Boolean = tradeClassOpt match {
        case Some(tc: BusinessTradeClass) => false
        case _                            => true
      }

      def seasonalBusIsMissing: Boolean = seasonalOpt.isEmpty

      def tradeClassIsOther: Boolean = tradeClassOpt match {
        case Some(BusinessTradeClass.Other) => true
        case _                              => false
      }

      def otherDescIsMissing: Boolean = stringMissing(otherDescOpt)

      val isGroupMember =
        request.userAnswers.get(GroupMemberPage) match {
          case Some(value) => value
          case _ => false
        }

      if (tradeClassIsMissing && !isGroupMember) {
          Redirect(routes.BusinessTradeClassController.onPageLoad(NormalMode))
      } else if (tradeClassIsOther && otherDescIsMissing && !isGroupMember) {
          Redirect(routes.OtherTradeClassController.onPageLoad(NormalMode))
      } else if (seasonalBusIsMissing) {
          Redirect(routes.SeasonalBusinessController.onPageLoad(NormalMode))
      } else {
          Redirect(routes.ChangeRegistrationDetailsController.onPageLoad())
        }
      }

      }


}
