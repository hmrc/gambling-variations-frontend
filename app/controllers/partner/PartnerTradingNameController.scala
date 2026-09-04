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

package controllers.partner

import controllers.actions.*
import controllers.partner.PartnerUtils.getIndex
import controllers.routes
import forms.partner.PartnerTradingNameFormProvider
import models.Mode
import navigation.Navigator
import pages.partnerdetails.PartnerDetailsTradingNamePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PartnerUtils.getPartnersSize
import views.html.partner.PartnerTradingNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerTradingNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerTradingNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerTradingNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      val index: Int = request.userAnswers.getIndex

      request.userAnswers.get(PartnerDetailsTradingNamePage(index)) match {
        case Some(partnerTradingName) =>
          val preparedForm = form.fill(partnerTradingName)

          Ok(view(preparedForm, mode))

        case None =>
          Redirect(routes.SystemErrorController.onPageLoad())
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      val index: Int = request.userAnswers.getIndex

      request.userAnswers.get(PartnerDetailsTradingNamePage(index)) match {
        case Some(_) =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(view(formWithErrors, mode))
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(
                                        PartnerDetailsTradingNamePage(index),
                                        value
                                      )
                                    )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(
                    PartnerDetailsTradingNamePage(index),
                    mode,
                    updatedAnswers
                  )
                )
            )

        case None =>
          Future.successful(
            Redirect(routes.SystemErrorController.onPageLoad())
          )
      }
    }
}
