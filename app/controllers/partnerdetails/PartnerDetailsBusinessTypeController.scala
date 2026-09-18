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

package controllers.partnerdetails

import controllers.actions.*
import forms.partnerdetails.PartnerDetailsBusinessTypeFormProvider
import models.{BusinessType, Mode}
import navigation.Navigator
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsBusinessTypePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PartnerUtils
import views.html.partnerdetails.PartnerDetailsBusinessTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

//TODO this controller has changed, note the route for QA
class PartnerDetailsBusinessTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerDetailsBusinessTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsBusinessTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[BusinessType] = formProvider()

  def onPageLoad(index: String, mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    // TODO this is an example of making sure index for newPartner is correct
    // It could act as a fail-safe is user is screwing around the index number to make a mess,
    // but with it, we can also just remove index from being passed around (for new Partners, still needed for existing)
//    if !PartnerUtils.isIndexCorrect(index, mode, request.userAnswers) then {
//      Redirect(routes.PartnerDetailsBusinessTypeController.onPageLoad(PartnerUtils.findIndexForNewPartner(request.userAnswers).toString, mode))
//    } else {

    val newIndex = PartnerUtils.parseIndex(index, mode, request.userAnswers)

    val preparedForm = request.userAnswers.get(PartnerDetailsBusinessTypePage(newIndex)) match {
      case None               => form
      case Some(businessType) => form.fill(businessType)
    }

    Ok(view(preparedForm, index, mode))
//    }
  }

  def onSubmit(index: String, mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val newIndex = PartnerUtils.parseIndex(index, mode)

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, index, mode))),
        businessType =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerDetailsBusinessTypePage(newIndex), businessType))
            // TODO uncomment it and decide if existing partner should modify it too
            // updatedAnswers <- Future.fromTry(updatedAnswers.set(PartnerDetailsAddPartnerCompletedPage(-1), false))
            updatedAnswers <-
              newIndex match {
                case _: String            => Future.successful(updatedAnswers)
                case newPartnerIndex: Int => Future.fromTry(updatedAnswers.set(PartnerDetailsAddPartnerCompletedPage(newPartnerIndex), false))
              }
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PartnerDetailsBusinessTypePage(newIndex), mode, updatedAnswers))
      )
  }
}
