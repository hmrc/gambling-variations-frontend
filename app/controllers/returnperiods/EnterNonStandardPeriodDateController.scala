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
import controllers.routes
import forms.returnperiods.EnterNonStandardPeriodDateFormProvider
import models.{BusinessDetails, GamblingReturnPeriods, Mode, NormalMode}
import navigation.Navigator
import pages.returnperiods.{ChooseReturnPeriodsPage, NonStandardPeriodDatePages}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.returnperiods.NstpPeriodCalculator
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returnperiods.EnterNonStandardPeriodDateView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnterNonStandardPeriodDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: GamblingReturnPeriodsDataRequiredAction,
  formProvider: EnterNonStandardPeriodDateFormProvider,
  nstpPeriodCalculator: NstpPeriodCalculator,
  gamblingConnector: GamblingConnector,
  clock: Clock,
  val controllerComponents: MessagesControllerComponents,
  view: EnterNonStandardPeriodDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val numberOfPeriods = 8

  def onPageLoad(
    periodNumber: Int,
    mode: Mode
  ): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      if (!validPeriodNumber(periodNumber)) {

        Future.successful(
          Redirect(routes.SystemErrorController.onPageLoad())
        )

      } else {

        request.userAnswers.get(ChooseReturnPeriodsPage) match {

          case None =>
            Future.successful(
              Redirect(routes.SystemErrorController.onPageLoad())
            )

          case Some(returnPeriodsId) =>
            for {
              businessDetails <-
                gamblingConnector.getBusinessDetails(request.mgdRegNum)

              gamblingReturnPeriods <-
                gamblingConnector.getGamblingReturnPeriods(request.mgdRegNum)

            } yield {

              val baseDate =
                calculateBaseDate(
                  businessDetails,
                  gamblingReturnPeriods
                )

              val periodEndDates =
                nstpPeriodCalculator.calculate(
                  baseDate,
                  returnPeriodsId
                )

              periodEndDates.lift(periodNumber - 1) match {

                case None =>
                  Redirect(
                    routes.SystemErrorController.onPageLoad()
                  )

                case Some(periodEnd) =>
                  val lowerBoundary =
                    periodEnd.minusDays(16)

                  val upperBoundary =
                    periodEnd.plusDays(16)

                  val form =
                    formProvider(
                      lowerBoundary,
                      upperBoundary
                    )

                  val page =
                    NonStandardPeriodDatePages(periodNumber)

                  val preparedForm =
                    request.userAnswers
                      .get(page)
                      .map(form.fill)
                      .getOrElse(form)

                  Ok(
                    view(
                      preparedForm,
                      mode,
                      periodNumber,
                      numberOfPeriods,
                      periodEnd,
                      lowerBoundary,
                      upperBoundary
                    )
                  )
              }
            }
        }
      }
    }

  def onSubmit(
    periodNumber: Int,
    mode: Mode
  ): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      if (!validPeriodNumber(periodNumber)) {

        Future.successful(
          Redirect(routes.SystemErrorController.onPageLoad())
        )

      } else {

        request.userAnswers
          .get(ChooseReturnPeriodsPage)
          .fold {
            Future.successful(
              Redirect(controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(NormalMode))
            )

          } { returnPeriodsId =>

            val page =
              NonStandardPeriodDatePages(periodNumber)

            for {
              businessDetails <-
                gamblingConnector.getBusinessDetails(request.mgdRegNum)

              gamblingReturnPeriods <-
                gamblingConnector.getGamblingReturnPeriods(request.mgdRegNum)

              result <- {

                val baseDate =
                  calculateBaseDate(
                    businessDetails,
                    gamblingReturnPeriods
                  )

                val periodEndDates =
                  nstpPeriodCalculator.calculate(
                    baseDate,
                    returnPeriodsId
                  )

                periodEndDates.lift(periodNumber - 1) match {

                  case None =>
                    Future.successful(
                      Redirect(
                        routes.SystemErrorController.onPageLoad()
                      )
                    )

                  case Some(periodEnd) =>
                    val lowerBoundary =
                      periodEnd.minusDays(16)

                    val upperBoundary =
                      periodEnd.plusDays(16)

                    val form =
                      formProvider(
                        lowerBoundary,
                        upperBoundary
                      )

                    form
                      .bindFromRequest()
                      .fold(
                        formWithErrors =>
                          Future.successful(
                            BadRequest(
                              view(
                                formWithErrors,
                                mode,
                                periodNumber,
                                numberOfPeriods,
                                periodEnd,
                                lowerBoundary,
                                upperBoundary
                              )
                            )
                          ),
                        value =>
                          for {
                            updatedAnswers <-
                              Future.fromTry(
                                request.userAnswers.set(
                                  page,
                                  value
                                )
                              )

                            _ <-
                              sessionRepository.set(updatedAnswers)

                          } yield {
                            Redirect(
                              navigator.nextPage(
                                page,
                                mode,
                                updatedAnswers
                              )
                            )
                          }
                      )
                }
              }

            } yield result
          }
      }
    }

  private def calculateBaseDate(
    businessDetails: BusinessDetails,
    gamblingReturnPeriods: GamblingReturnPeriods
  ): LocalDate = {

    val registrationDate =
      getRegistrationDate(businessDetails)

    gamblingReturnPeriods.hasExistingNstpValues match {

      case Some(true) =>
        gamblingReturnPeriods.nstpEndDate8 match {

          case Some(lastNstpDate) =>
            maxDate(
              lastNstpDate,
              registrationDate
            )

          case None =>
            maxDate(
              LocalDate.now(clock),
              registrationDate
            )
        }

      case _ =>
        maxDate(
          LocalDate.now(clock),
          registrationDate
        )
    }
  }

  private def getRegistrationDate(
    businessDetails: BusinessDetails
  ): LocalDate =
    businessDetails.dateOfRegistration.getOrElse(
      throw new RuntimeException("No registration date found")
    )

  private def maxDate(
    first: LocalDate,
    second: LocalDate
  ): LocalDate =
    if (first.isAfter(second)) {
      first
    } else {
      second
    }

  private def validPeriodNumber(
    periodNumber: Int
  ): Boolean =
    periodNumber >= 1 && periodNumber <= numberOfPeriods
}
