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
import controllers.actions.{AuthorisedAction, DataRetrievalAction, GamblingReturnPeriodsDataRequiredAction}
import models.requests.DataRequest
import models.{BusinessDetails, GamblingReturnPeriods, NormalMode}
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, NonStandardPeriodDatePages, ReturnPeriodsChoicePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.returnperiods.NstpPeriodCalculator
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returnperiods.CheckReturnPeriodsViewModel
import views.html.returnperiods.CheckReturnPeriodsView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckReturnPeriodsController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: GamblingReturnPeriodsDataRequiredAction,
  gamblingConnector: GamblingConnector,
  nstpPeriodCalculator: NstpPeriodCalculator,
  clock: Clock,
  val controllerComponents: MessagesControllerComponents,
  view: CheckReturnPeriodsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val numberOfPeriods = 8
  private val validReturnPeriodIds = Set(1, 2, 3)

  def onPageLoad(): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      CheckReturnPeriodsViewModel.from(request.userAnswers) match {
        case Some(standard) => Future.successful(Ok(view(standard)))
        case None           => nonStandardPage
      }
    }

  def onContinue(): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { _ =>
      Redirect(controllers.routes.ChangeRegistrationDetailsController.onPageLoad())
    }

  private def nonStandardPage(implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(ChooseReturnPeriodsPage) match {
      case None =>
        Future.successful(Redirect(routes.ChooseReturnPeriodsController.onPageLoad(NormalMode)))

      case Some(returnPeriodsId) if !validReturnPeriodIds.contains(returnPeriodsId) =>
        Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))

      case Some(returnPeriodsId) =>
        request.userAnswers.get(GamblingReturnPeriodsPage) match {
          case None =>
            Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))

          case Some(gamblingReturnPeriods) =>
            gamblingConnector.getBusinessDetails(request.mgdRegNum).map { businessDetails =>
              val viewModel =
                CheckReturnPeriodsViewModel.nonStandard(
                  returnPeriodsChoice  = request.userAnswers.get(ReturnPeriodsChoicePage),
                  returnPeriodsId      = returnPeriodsId,
                  calculatedPeriodEnds = nstpPeriodCalculator.calculate(calculateBaseDate(businessDetails, gamblingReturnPeriods), returnPeriodsId),
                  enteredDates = (1 to numberOfPeriods).map(periodNumber => request.userAnswers.get(NonStandardPeriodDatePages(periodNumber))),
                  showFinalPeriodWarning = gamblingReturnPeriods.finalPeriodWarning.contains(true)
                )(messagesApi.preferred(request))

              Ok(view(viewModel))
            }
        }
    }

  private def calculateBaseDate(businessDetails: BusinessDetails, gamblingReturnPeriods: GamblingReturnPeriods): LocalDate = {
    val registrationDate =
      businessDetails.dateOfRegistration.getOrElse(throw new RuntimeException("No registration date found"))

    val referenceDate =
      if (gamblingReturnPeriods.hasExistingNstpValues.contains(true)) {
        gamblingReturnPeriods.nstpEndDate8.getOrElse(LocalDate.now(clock))
      } else {
        LocalDate.now(clock)
      }

    if (referenceDate.isAfter(registrationDate)) referenceDate else registrationDate
  }
}
