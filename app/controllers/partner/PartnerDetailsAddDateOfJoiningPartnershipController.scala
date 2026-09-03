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
import controllers.partner.PartnerUtils.getPartnersSize
import controllers.routes
import forms.partner.PartnerDetailsAddDateOfJoiningPartnershipFormProvider
import models.Mode
import navigation.Navigator
import pages.partnerdetails.{PartnerDetailsDateOfIncorporation, PartnerDetailsDateOfJoiningPage}
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.dateTimeFormat
import views.html.partner.PartnerDetailsAddDateOfJoiningPartnershipView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerDetailsAddDateOfJoiningPartnershipController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerDetailsAddDateOfJoiningPartnershipFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsAddDateOfJoiningPartnershipView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val TwoWeeks: Int = 14
  private val formatter = dateTimeFormat()(Lang("en"))

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val newIndex = request.userAnswers.getPartnersSize

    request.userAnswers.get(PartnerDetailsDateOfJoiningPage(newIndex)) match {
      case None =>
        Redirect(routes.SystemErrorController.onPageLoad())
      case Some(dateOfJoining) =>
        val form = request.userAnswers
          .get(PartnerDetailsDateOfIncorporation(newIndex))
          .fold(formProvider(dateOfJoining))(formProvider(dateOfJoining).fill)

        val dateOfJoiningFormatted = dateOfJoining.format(formatter)
        val twoWeeksLaterFormatted = dateOfJoining.plusDays(TwoWeeks).format(formatter)

        Ok(view(form, mode, dateOfJoiningFormatted, twoWeeksLaterFormatted))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    val newIndex = request.userAnswers.getPartnersSize

    request.userAnswers.get(PartnerDetailsDateOfJoiningPage(newIndex)) match {
      case None =>
        Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
      case Some(dateOfJoining) =>
        request.userAnswers
          .get(PartnerDetailsDateOfIncorporation(newIndex))
          .fold(formProvider(dateOfJoining))(formProvider(dateOfJoining).fill)
          .bindFromRequest()
          .fold(
            formWithErrors =>
              val dateOfJoiningFormatted = dateOfJoining.format(formatter)
              val twoWeeksLaterFormatted = dateOfJoining.plusDays(TwoWeeks).format(formatter)
              Future.successful(BadRequest(view(formWithErrors, mode, dateOfJoiningFormatted, twoWeeksLaterFormatted)))
            ,
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerDetailsDateOfIncorporation(newIndex), value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(PartnerDetailsDateOfIncorporation(newIndex), mode, updatedAnswers))
          )
    }
  }
}
