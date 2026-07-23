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
import forms.CorrespondenceUKAddressFormProvider
import models.Mode
import navigation.Navigator
import pages.{CorrespondenceAddressUkPage, CorrespondenceDetailsSubmittedPage, isleMOrChannelFlagPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CorrespondenceUKAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CorrespondenceUKAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: CorrespondenceDetailsDataRequiredAction,
  formProvider: CorrespondenceUKAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CorrespondenceUKAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  private def isIomOrCiPostcode(postcode: String): Boolean = {
    val normalised = postcode.trim.toUpperCase

    normalised.startsWith("IM") ||
    normalised.startsWith("JE") ||
    normalised.startsWith("GY")
  }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      val preparedForm = request.userAnswers
        .get(CorrespondenceAddressUkPage)
        .fold(form)(form.fill)

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

            val iomOrCiFlag =
              if (value.postcode.exists(isIomOrCiPostcode)) {
                "true"
              } else {
                "false"
              }

            for {
              updatedAnswers <- Future.fromTry(
                                  request.userAnswers.set(
                                    CorrespondenceAddressUkPage,
                                    value
                                  )
                                )

              updatedAnswers <- Future.fromTry(
                                  updatedAnswers.set(
                                    isleMOrChannelFlagPage,
                                    iomOrCiFlag
                                  )
                                )

              updatedAnswers <- Future.fromTry(
                                  updatedAnswers.set(
                                    CorrespondenceDetailsSubmittedPage,
                                    true
                                  )
                                )

              _ <- sessionRepository.set(updatedAnswers)

            } yield Redirect(
              navigator.nextPage(
                CorrespondenceAddressUkPage,
                mode,
                updatedAnswers
              )
            )
          }
        )
    }
}
