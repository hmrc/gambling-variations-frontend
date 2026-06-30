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
import forms.RemoveCorrespondenceFaxNumberFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import utils.FlagsUtil.checkIfChanged
import pages.{CorrespondenceDetailsChangesPage, CorrespondenceDetailsSubmittedPage, CorrespondenceFaxNumberPage, RemoveCorrespondenceFaxNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveCorrespondenceFaxNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class RemoveCorrespondenceFaxNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: CorrespondenceDetailsDataRequiredAction,
  formProvider: RemoveCorrespondenceFaxNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveCorrespondenceFaxNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      request.userAnswers.get(CorrespondenceFaxNumberPage) map { correspondenceFaxNumber =>

        val preparedForm = request.userAnswers.get(RemoveCorrespondenceFaxNumberPage) match {
          case Some(value) => form.fill(value)
          case None        => form
        }

        Ok(view(preparedForm, mode, correspondenceFaxNumber))

      } getOrElse Redirect(routes.SystemErrorController.onPageLoad())
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      request.userAnswers.get(CorrespondenceFaxNumberPage) map { correspondenceFaxNumber =>

        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, correspondenceFaxNumber))),
            value =>
              val isChanged: Boolean =
                checkIfChanged(value, request.userAnswers, RemoveCorrespondenceFaxNumberPage)
              for {
                updatedAnswers <- Future.fromTry(updateUserAnswers(request.userAnswers, value))
                updatedAnswers <- Future.fromTry(updatedAnswers.set(CorrespondenceDetailsChangesPage, isChanged))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(
                navigator.nextPage(RemoveCorrespondenceFaxNumberPage, mode, updatedAnswers)
              )
          )

      } getOrElse Future.successful(
        Redirect(routes.SystemErrorController.onPageLoad())
      )
    }

  private def updateUserAnswers(
    userAnswers: UserAnswers,
    value: Boolean
  ): Try[UserAnswers] = {

    for {
      ua <- userAnswers.set(CorrespondenceDetailsSubmittedPage, true)
      ua1 <- {
        if (value) {
          ua.remove(CorrespondenceFaxNumberPage)
        } else {
          Try(ua)
        }
      }
    } yield ua1
  }
}
