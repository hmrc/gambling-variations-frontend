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

package controllers.cancellation

import controllers.actions.*
import controllers.routes
import forms.cancellation.WarningForCancellationFormProvider
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.cancellation.{RegistrationCancellationDatePage, RegistrationCancellationDeclarationPage, WarningForCancellationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.cancellation.WarningForCancellationView

import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WarningForCancellationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: BusinessDetailsDataRequiredAction,
  formProvider: WarningForCancellationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WarningForCancellationView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      val form = request.userAnswers.get(WarningForCancellationPage).fold(formProvider())(formProvider().fill)

      (for {
        isCancelled      <- request.userAnswers.get(RegistrationCancellationDeclarationPage).filter(identity)
        cancellationDate <- request.userAnswers.get(RegistrationCancellationDatePage)
      } yield Ok(view(form, dateFormatter.format(cancellationDate), NormalMode)))
        .getOrElse(Redirect(routes.SystemErrorController.onPageLoad().url))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = {
    (authorise andThen getData andThen requireData).async { implicit request =>
      val form = formProvider()
      val cancellationDate = request.userAnswers.get(RegistrationCancellationDatePage)

      cancellationDate
        .map(systemDate =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, dateFormatter.format(systemDate), mode)
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(WarningForCancellationPage, value)
                                    )

                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(RegistrationCancellationDeclarationPage, false)
                                    )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(RegistrationCancellationDeclarationPage, mode, updatedAnswers)
                )
            )
        )
        .getOrElse(Future.successful(Redirect(routes.SystemErrorController.onPageLoad().url)))
    }
  }

  private val dateFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy")
}
