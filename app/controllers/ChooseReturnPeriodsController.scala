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

import controllers.actions._
import forms.ChooseReturnPeriodsFormProvider
import models.{ChooseReturnPeriods, Mode, ReturnPeriodsVariant, UserAnswers}
import navigation.Navigator
import pages.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ChooseReturnPeriodsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChooseReturnPeriodsController @Inject()(
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

      val variant = variantFrom(request.userAnswers)
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
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async {
      implicit request =>

        val variant = variantFrom(request.userAnswers)
        val form = formProvider(variant.errorMessageKey)

        form.bindFromRequest().fold(

          formWithErrors =>
            Future.successful(
              BadRequest(
                view(formWithErrors, mode, variant)
              )
            ),

          value =>
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(
                    ChooseReturnPeriodsPage,
                    value.returnPeriodsId
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
        )
    }

  private def variantFrom(
                           userAnswers: UserAnswers
                         ): ReturnPeriodsVariant =
    userAnswers
      .get(GamblingReturnPeriodsPage)
      .map(_.hasExistingNstpValues.get)
      .map {
        case true  => ReturnPeriodsVariant.NonStandard
        case false => ReturnPeriodsVariant.Standard
      }
      .get
}
