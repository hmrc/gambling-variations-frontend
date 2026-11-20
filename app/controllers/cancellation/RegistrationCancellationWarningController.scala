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

import controllers.actions.{AuthorisedAction, BusinessDetailsDataRequiredAction, DataRetrievalAction}
import controllers.routes
import forms.cancellation.RegistrationCancellationWarningFormProvider
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.cancellation.{RegistrationCancellationCancelAgainYesNoPage, RegistrationCancellationDatePage, RegistrationCancellationDeclarationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.cancellation.RegistrationCancellationWarningView

import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationCancellationWarningController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: BusinessDetailsDataRequiredAction,
  formProvider: RegistrationCancellationWarningFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RegistrationCancellationWarningView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with CancellationEntrypointDecider
    with I18nSupport {

  // TODO extra check
  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    checkScreen(request.userAnswers, CurrentScreen.Warning) {
      request.userAnswers
        .get(RegistrationCancellationDatePage)
        .fold(Redirect(routes.SystemErrorController.onPageLoad())) { cancellationDate =>
          val form = request.userAnswers
            .get(RegistrationCancellationCancelAgainYesNoPage)
            .fold(formProvider())(formProvider().fill)

          Ok(view(form, dateFormatter.format(cancellationDate), NormalMode))
        }
    }
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
                                      request.userAnswers.set(RegistrationCancellationCancelAgainYesNoPage, value)
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
