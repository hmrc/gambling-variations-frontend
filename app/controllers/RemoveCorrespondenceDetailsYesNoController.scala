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

import controllers.actions.*
import forms.RemoveCorrespondenceDetailsYesNoFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveCorrespondenceDetailsYesNoView

import scala.concurrent.{ExecutionContext, Future}

class RemoveCorrespondenceDetailsYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: CorrespondenceDetailsDataRequiredAction,
  formProvider: RemoveCorrespondenceDetailsYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveCorrespondenceDetailsYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(RemoveCorrespondenceDetailsYesNoPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, mode))
            ),
          value => {

            val updatedAnswers =
              if (value) {
                for {
                  updatedAnswers <- request.userAnswers.remove(CorrespondenceNamePage)
                  updatedAnswers <- updatedAnswers.remove(CorrespondenceAdditionalNamePage)
                  updatedAnswers <- updatedAnswers.remove(CorrespondenceAddressUkPage)
                  updatedAnswers <- updatedAnswers.remove(CorrespondenceAddressNonUkPage)
                  updatedAnswers <- updatedAnswers.remove(CorrespondenceAdditionalInformationPage)
                  updatedAnswers <- updatedAnswers.remove(isleMOrChannelFlagPage)
                  updatedAnswers <- updatedAnswers.remove(CorrespondenceContactNumberPage)
                  updatedAnswers <- updatedAnswers.remove(CorrespondenceFaxNumberPage)
                  updatedAnswers <- updatedAnswers.remove(CorrespondenceEmailPage)
                  updatedAnswers <- updatedAnswers.remove(AddCorrespondenceAdditionalNamePage)
                  updatedAnswers <- updatedAnswers.set(RemoveCorrespondenceDetailsYesNoPage, value)
                  updatedAnswers <- updatedAnswers.set(CorrespondenceDetailsChangesPage, value)
                } yield updatedAnswers
              } else {
                request.userAnswers.set(RemoveCorrespondenceDetailsYesNoPage, value)
              }

            Future
              .fromTry(updatedAnswers)
              .flatMap { answers =>
                sessionRepository.set(answers).map { _ =>
                  Redirect(
                    navigator.nextPage(
                      RemoveCorrespondenceDetailsYesNoPage,
                      mode,
                      answers
                    )
                  )
                }
              }
          }
        )
    }
}
