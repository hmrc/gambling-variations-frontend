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

package controllers.returnperiods

import connectors.GamblingConnector
import controllers.actions.*
import models.{BusinessDetails, GamblingReturnPeriods, NormalMode}
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, NonStandardPeriodDatePages, ReturnPeriodsChoicePage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.returnperiods.NstpPeriodCalculator
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returnperiods.CheckNonStandardReturnPeriodsViewModel
import views.html.returnperiods.CheckNonStandardReturnPeriodsView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckNonStandardReturnPeriodsController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: GamblingReturnPeriodsDataRequiredAction,
  gamblingConnector: GamblingConnector,
  nstpPeriodCalculator: NstpPeriodCalculator,
  clock: Clock,
  val controllerComponents: MessagesControllerComponents,
  view: CheckNonStandardReturnPeriodsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val numberOfPeriods = 8
  private val validReturnPeriodIds = Set(1, 2, 3)

  def onPageLoad: Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(ChooseReturnPeriodsPage) match {
        case None =>
          Future.successful(
            Redirect(
              controllers.returnperiods.routes.ChooseReturnPeriodsController
                .onPageLoad(NormalMode)
            )
          )

        case Some(returnPeriodsId) if !validReturnPeriodIds.contains(returnPeriodsId) =>
          Future.successful(
            Redirect(controllers.routes.SystemErrorController.onPageLoad())
          )

        case Some(returnPeriodsId) =>
          val returnPeriodsChoice =
            request.userAnswers.get(ReturnPeriodsChoicePage)

          request.userAnswers.get(GamblingReturnPeriodsPage) match {
            case None =>
              Future.successful(
                Redirect(controllers.routes.SystemErrorController.onPageLoad())
              )

            case Some(gamblingReturnPeriods) =>
              gamblingConnector.getBusinessDetails(request.mgdRegNum).map { businessDetails =>
                implicit val messages: Messages = messagesApi.preferred(request)

                val baseDate =
                  calculateBaseDate(businessDetails, gamblingReturnPeriods)

                val calculatedPeriodEnds =
                  nstpPeriodCalculator.calculate(baseDate, returnPeriodsId)

                val enteredDates =
                  (1 to numberOfPeriods).map { periodNumber =>
                    request.userAnswers.get(
                      NonStandardPeriodDatePages(periodNumber)
                    )
                  }

                val showFinalPeriodWarning =
                  gamblingReturnPeriods.finalPeriodWarning.contains(true)

                val viewModel =
                  CheckNonStandardReturnPeriodsViewModel(
                    returnPeriodsChoice    = returnPeriodsChoice,
                    returnPeriodsId        = returnPeriodsId,
                    calculatedPeriodEnds   = calculatedPeriodEnds,
                    enteredDates           = enteredDates,
                    showFinalPeriodWarning = showFinalPeriodWarning
                  )

                Ok(view(viewModel))
              }
          }
      }
    }

  private def calculateBaseDate(
    businessDetails: BusinessDetails,
    gamblingReturnPeriods: GamblingReturnPeriods
  ): LocalDate = {

    val registrationDate =
      businessDetails.dateOfRegistration.getOrElse {
        throw new RuntimeException("No registration date found")
      }

    val referenceDate =
      if (gamblingReturnPeriods.hasExistingNstpValues.contains(true)) {
        gamblingReturnPeriods.nstpEndDate8
          .getOrElse(LocalDate.now(clock))
      } else {
        LocalDate.now(clock)
      }

    if (referenceDate.isAfter(registrationDate)) {
      referenceDate
    } else {
      registrationDate
    }
  }
}
