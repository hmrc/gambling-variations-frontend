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

import controllers.actions.*
import forms.returnperiods.ChooseReturnPeriodsFormProvider
import models.{ChooseReturnPeriods, Mode, ReturnPeriodsVariant, UserAnswers, WhatToDoWithStandardReturnPeriods}
import navigation.Navigator
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, HasExistingNstpValuesPage, WhatToDoWithStandardReturnPeriodsPage}
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, HasExistingNstpValuesPage, NonStandardPeriodDate1Page, NonStandardPeriodDate2Page, NonStandardPeriodDate3Page, NonStandardPeriodDate4Page, NonStandardPeriodDate5Page, NonStandardPeriodDate6Page, NonStandardPeriodDate7Page, NonStandardPeriodDate8Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returnperiods.ChooseReturnPeriodsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class ChooseReturnPeriodsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: GamblingReturnPeriodsDataRequiredAction,
  formProvider: ChooseReturnPeriodsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ChooseReturnPeriodsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      variantFrom(request.userAnswers) match {

        case Some(variant) =>
          val form = formProvider(variant.errorMessageKey)

          val preparedForm =
            request.userAnswers.get(ChooseReturnPeriodsPage) match {

              case Some(returnPeriodsId) =>
                ChooseReturnPeriods
                  .fromReturnPeriodsId(returnPeriodsId)
                  .map(form.fill)
                  .getOrElse(form)

              case None =>
                request.userAnswers
                  .get(GamblingReturnPeriodsPage)
                  .flatMap(_.returnPeriodsId)
                  .flatMap(ChooseReturnPeriods.fromReturnPeriodsId)
                  .map(form.fill)
                  .getOrElse(form)
            }

          Ok(view(preparedForm, mode, variant))

        case None =>
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      variantFrom(request.userAnswers) match {

        case Some(variant) =>
          val form = formProvider(variant.errorMessageKey)

          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, mode, variant)
                  )
                ),
              value => {

                val previousReturnPeriodsId =
                  request.userAnswers.get(ChooseReturnPeriodsPage)

                val answers =
                  if (previousReturnPeriodsId.exists(_ != value.returnPeriodsId)) {
                    clearNstpDates(request.userAnswers)
                  } else {
                    Try(request.userAnswers)
                  }

                for {
                  answersAfterClear <- Future.fromTry(answers)

                  updatedAnswers <-
                    Future.fromTry(
                      answersAfterClear
                        .set(
                          ChooseReturnPeriodsPage,
                          value.returnPeriodsId
                        )
                        .flatMap(
                          _.set(
                            HasExistingNstpValuesPage,
                            variant == ReturnPeriodsVariant.NonStandard
                          )
                        )
                    )

                  _ <- sessionRepository.set(updatedAnswers)

                } yield Redirect(
                  navigator.nextPage(
                    ChooseReturnPeriodsPage,
                    mode,
                    updatedAnswers
                  )
                )
              }
            )

        case None =>
          Future.successful(
            Redirect(controllers.routes.SystemErrorController.onPageLoad())
          )
      }
    }

  private def clearNstpDates(
    userAnswers: UserAnswers
  ): Try[UserAnswers] =
    userAnswers
      .remove(NonStandardPeriodDate1Page)
      .flatMap(_.remove(NonStandardPeriodDate2Page))
      .flatMap(_.remove(NonStandardPeriodDate3Page))
      .flatMap(_.remove(NonStandardPeriodDate4Page))
      .flatMap(_.remove(NonStandardPeriodDate5Page))
      .flatMap(_.remove(NonStandardPeriodDate6Page))
      .flatMap(_.remove(NonStandardPeriodDate7Page))
      .flatMap(_.remove(NonStandardPeriodDate8Page))

  private def variantFrom(
    userAnswers: UserAnswers
  ): Option[ReturnPeriodsVariant] = {
    userAnswers
      .get(GamblingReturnPeriodsPage)
      .flatMap(_.hasExistingNstpValues)
      .map {

        case true =>
          ReturnPeriodsVariant.NonStandard

        case false =>
          userAnswers
            .get(WhatToDoWithStandardReturnPeriodsPage) match {

            case Some(WhatToDoWithStandardReturnPeriods.Switchtononstandard) =>
              ReturnPeriodsVariant.NonStandard

            case _ =>
              ReturnPeriodsVariant.Standard
          }
      }
  }
}
