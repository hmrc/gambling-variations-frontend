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
                  updatedAnswer  <- request.userAnswers.remove(CorrespondenceNamePage)
                  updatedAnswer  <- updatedAnswer.remove(CorrespondenceAdditionalNamePage)
                  updatedAnswer  <- updatedAnswer.remove(CorrespondenceAddressUkPage)
                  updatedAnswer  <- updatedAnswer.remove(CorrespondenceAddressNonUkPage)
                  updatedAnswer  <- updatedAnswer.remove(CorrespondenceAdditionalInformationPage)
                  updatedAnswer  <- updatedAnswer.remove(isleMOrChannelFlagPage)
                  updatedAnswer  <- updatedAnswer.remove(CorrespondenceContactNumberPage)
                  updatedAnswer  <- updatedAnswer.remove(CorrespondenceFaxNumberPage)
                  updatedAnswer  <- updatedAnswer.remove(CorrespondenceEmailPage)
                  updatedAnswer  <- updatedAnswer.remove(AddCorrespondenceAdditionalNamePage)
                  updatedAnswer  <- updatedAnswer.set(RemoveCorrespondenceDetailsYesNoPage, value)
                  updatedAnswers <- updatedAnswer.set(CorrespondenceDetailsChangesPage, value)
                } yield updatedAnswer
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
