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
import forms.partnerdetails.PartnerDetailsRemovePartnerYesNoFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.BusinessNumberOrIndex
import pages.partnerdetails.{PartnerDetailsBusinessNamePage, PartnerDetailsChosenPartnerToRemovePage, PartnerDetailsDateOfLeavingPage, PartnerDetailsRemovePartnerYesNoPage, PartnerDetailsSoleProprietorPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsArray, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PartnerUtils
import views.html.partnerdetails.PartnerDetailsRemovePartnerYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class PartnerDetailsRemovePartnerYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerDetailsRemovePartnerYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsRemovePartnerYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  private def partnerName(userAnswers: UserAnswers, index: BusinessNumberOrIndex): Option[String] =
    userAnswers
      .get(PartnerDetailsBusinessNamePage(index))
      .orElse(userAnswers.get(PartnerDetailsSoleProprietorPage(index)).map(_.fullName))

  def onPageLoad(index: String, mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val newIndex = PartnerUtils.parseIndex(index, mode)

    partnerName(request.userAnswers, newIndex) match {
      case Some(name) =>
        val preparedForm = request.userAnswers.get(PartnerDetailsRemovePartnerYesNoPage(newIndex)).fold(form)(form.fill)
        Ok(view(preparedForm, index, mode, name))
      case None =>
        Redirect(controllers.routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(index: String, mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val newIndex = PartnerUtils.parseIndex(index, mode)

    partnerName(request.userAnswers, newIndex) match {
      case None =>
        Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))

      case Some(name) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, index, mode, name))),
            wantToRemove =>
              if (!wantToRemove)
                Future.successful(Redirect(navigator.nextPage(PartnerDetailsRemovePartnerYesNoPage(newIndex), mode, request.userAnswers)))
              else
                newIndex match {
                  case businessNumber: String =>
                    for {
                      updated <- Future.fromTry(request.userAnswers.set(PartnerDetailsChosenPartnerToRemovePage, businessNumber))
                      _       <- sessionRepository.set(updated)
                    } yield Redirect(navigator.nextPage(PartnerDetailsRemovePartnerYesNoPage(newIndex), mode, updated))

                  case newPartnerIndex: Int =>
                    for {
                      updated <- Future.fromTry(removeNewPartner(request.userAnswers, newPartnerIndex))
                      _       <- sessionRepository.set(updated)
                    } yield Redirect(navigator.nextPage(PartnerDetailsRemovePartnerYesNoPage(newPartnerIndex), mode, updated))
                }
          )
    }
  }

  private def removeNewPartner(userAnswers: UserAnswers, index: Int): Try[UserAnswers] = {
    val newPartners = (userAnswers.data \ "newPartners").asOpt[JsArray].getOrElse(JsArray())

    if (index < 0 || index >= newPartners.value.size)
      Failure(new IndexOutOfBoundsException(s"No new partner at index $index"))
    else
      Success(userAnswers.copy(data = userAnswers.data ++ Json.obj("newPartners" -> JsArray(newPartners.value.patch(index, Nil, 1)))))
  }
}
