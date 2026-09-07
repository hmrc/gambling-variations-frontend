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
import models.requests.{DataRequest, OptionalDataRequest}
import navigation.Navigator
import pages.DateOfRegistrationPage
import pages.partnerdetails.{PartnerDetailsDateOfIncorporation, PartnerDetailsDateOfJoiningPage}
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{Action, ActionRefiner, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.dateTimeFormat
import views.html.partner.PartnerDetailsAddDateOfJoiningPartnershipView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerDetailsAddDateOfJoiningPartnershipController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  businessDetailsDataRequiredAction: BusinessDetailsDataRequiredAction,
  formProvider: PartnerDetailsAddDateOfJoiningPartnershipFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsAddDateOfJoiningPartnershipView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val formatter = dateTimeFormat()(Lang("en"))
  private val TWO_WEEKS: Int = 14

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData andThen transformToOptional andThen businessDetailsDataRequiredAction) { implicit request =>

      val newIndex = request.userAnswers.getPartnersSize

      request.userAnswers.get(DateOfRegistrationPage) match {
        case None =>
          Redirect(routes.SystemErrorController.onPageLoad())
        case Some(userDateOfRegistration) =>
          val twoWeeksFromTodayOrRegistrationDay = getRegistrationThreshold(request.userAnswers.get(DateOfRegistrationPage))

          val form = request.userAnswers
            .get(PartnerDetailsDateOfIncorporation(newIndex))
            .fold(formProvider(userDateOfRegistration, twoWeeksFromTodayOrRegistrationDay))(
              formProvider(userDateOfRegistration, twoWeeksFromTodayOrRegistrationDay).fill
            )

          val dateOfJoiningFormatted = userDateOfRegistration.format(formatter)
          val twoWeeksLaterFormatted = twoWeeksFromTodayOrRegistrationDay.format(formatter)

          Ok(view(form, mode, dateOfJoiningFormatted, twoWeeksLaterFormatted))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData andThen transformToOptional andThen businessDetailsDataRequiredAction).async { implicit request =>

      val newIndex = request.userAnswers.getPartnersSize

      request.userAnswers.get(DateOfRegistrationPage) match {
        case None =>
          Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
        case Some(userDateOfRegistration) =>
          val twoWeeksFromTodayOrRegistrationDay = getRegistrationThreshold(request.userAnswers.get(DateOfRegistrationPage))

          request.userAnswers
            .get(PartnerDetailsDateOfIncorporation(newIndex))
            .fold(formProvider(userDateOfRegistration, twoWeeksFromTodayOrRegistrationDay))(
              formProvider(userDateOfRegistration, twoWeeksFromTodayOrRegistrationDay).fill
            )
            .bindFromRequest()
            .fold(
              formWithErrors =>
                val dateOfJoiningFormatted = userDateOfRegistration.format(formatter)
                val twoWeeksLaterFormatted = twoWeeksFromTodayOrRegistrationDay.format(formatter)
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

  // Either today plus 14 days or dateOfRegistration plus 14 days if the date is in the future
  private def getRegistrationThreshold(userRegistrationDate: Option[LocalDate]): LocalDate = {
    val today = LocalDate.now()

    userRegistrationDate.fold(today)(date => if date.isAfter(today) then date else today).plusDays(TWO_WEEKS)
  }

  private val transformToOptional: ActionRefiner[DataRequest, OptionalDataRequest] =
    new ActionRefiner[DataRequest, OptionalDataRequest] {
      override protected def refine[A](
        request: DataRequest[A]
      ): Future[Either[Result, OptionalDataRequest[A]]] =
        Future.successful(
          Right(
            OptionalDataRequest(
              request     = request.request,
              mgdRegNum   = request.mgdRegNum,
              userAnswers = Some(request.userAnswers)
            )
          )
        )

      override protected def executionContext: ExecutionContext = ec
    }

}
