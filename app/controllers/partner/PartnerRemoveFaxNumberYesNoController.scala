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
import controllers.routes
import forms.partner.PartnerRemoveFaxNumberYesNoFormProvider
import models.requests.DataRequest
import models.{Address, CorrespondenceDetails, Mode}
import navigation.Navigator
import pages.CorrespondenceDetailsChangesPage
import pages.partner.PartnerRemoveFaxNumberYesNoPage
import pages.partnerdetails.PartnerDetailsCorrespondenceDetailsSectionPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Reads
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PartnerRemoveFaxNumberYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerRemoveFaxNumberYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerRemoveFaxNumberYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerRemoveFaxNumberYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request: DataRequest[AnyContent] =>

    val preparedForm = request.userAnswers.get(PartnerRemoveFaxNumberYesNoPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    // TODO: This index is hardcoded but it should come from the Partner Details list selection
    val index: Int = 0

    request.userAnswers
      .get(PartnerDetailsCorrespondenceDetailsSectionPage(index))
      .flatMap(_.faxNumber) match {
      case Some(faxNumber) =>
        Ok(view(preparedForm, mode, faxNumber))

      case None =>
        Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              view(formWithErrors,
                   mode,
                   request.userAnswers
                     .get(PartnerDetailsCorrespondenceDetailsSectionPage(0))
                     .flatMap(_.faxNumber)
                     .getOrElse("")
                  )
            )
          ),
        value => {
          val result = for {
            answersWithSelection <- Future.fromTry(request.userAnswers.set(PartnerRemoveFaxNumberYesNoPage, value))
            // TODO: This index is hardcoded but it should come from the Partner Details list selection
            index: Int = 0
            cleanedAnswers <- if (value) {
                                answersWithSelection.get(PartnerDetailsCorrespondenceDetailsSectionPage(index)) match {
                                  case Some(details) =>
                                    val updatedDetails = details.copy(faxNumber = None)
                                    Future.fromTry(answersWithSelection.set(PartnerDetailsCorrespondenceDetailsSectionPage(index), updatedDetails))

                                  case None =>
                                    Future.failed(new NoSuchElementException("Correspondence details section not found"))
                                }
                              } else {
                                Future.successful(answersWithSelection)
                              }

            finalAnswers <- Future.fromTry(cleanedAnswers.set(CorrespondenceDetailsChangesPage, value))
            _            <- sessionRepository.set(finalAnswers)
          } yield finalAnswers

          result.map { updatedAnswers =>
            Redirect(navigator.nextPage(PartnerRemoveFaxNumberYesNoPage, mode, updatedAnswers))
          }
        }
      )
  }
}
