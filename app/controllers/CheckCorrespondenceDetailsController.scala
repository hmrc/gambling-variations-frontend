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
import models.UserAnswers
import pages.*
import pages.correspondencedetails.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FlagsUtil.checkFlag
import viewmodels.CheckCorrespondenceDetailsViewModel
import views.html.CheckCorrespondenceDetailsView

import javax.inject.Inject

class CheckCorrespondenceDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: CorrespondenceDetailsDataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckCorrespondenceDetailsView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val answers = request.userAnswers

    if (!hasAnyCorrespondenceDetails(answers)) {
      Redirect(controllers.routes.AddCorrespondingDetailsYesNoController.onPageLoad())
    } else {
      Ok(
        view(
          CheckCorrespondenceDetailsViewModel(
            correspondenceName                     = answers.get(CorrespondenceNamePage),
            addCorrespondenceAdditionalName        = answers.get(CorrespondenceAdditionalNameYesNoPage),
            additionalCorrespondenceName           = answers.get(CorrespondenceAdditionalNamePage),
            correspondenceAddress                  = answers.get(CorrespondenceAddressUkPage) orElse answers.get(CorrespondenceAddressNonUkPage),
            addCorrespondenceAdditionalInformation = answers.get(AddCorrespondenceAddressAdditionalInformationPage),
            correspondenceAdditionalInformation    = answers.get(CorrespondenceAdditionalInformationPage),
            phoneNumber                            = answers.get(CorrespondenceContactNumberPage).flatMap(_.phoneNumber),
            mobilePhoneNumber                      = answers.get(CorrespondenceContactNumberPage).flatMap(_.mobilePhoneNumber),
            addCorrespondenceFaxNumber             = answers.get(AddCorrespondenceFaxNumberPage),
            faxNumber                              = answers.get(CorrespondenceFaxNumberPage),
            addCorrespondenceEmailAddress          = answers.get(AddEmailAddressForCorrespondenceYesNoPage),
            emailAddress                           = answers.get(CorrespondenceEmailPage),
            hasUkPostcode                          = answers.get(CorrespondenceUKAddrScreenerPage),
            isSubmitted                            = checkFlag(answers, CorrespondenceDetailsChangesPage, CorrespondenceDetailsSubmittedPage),
            isAddingNewCorrespondenceDetails       = answers.get(IsAddingNewCorrespondenceDetailsPage),
            changeCorrespondenceAddress            = answers.get(CorrespondenceChangeAddrScreenerPage)
          )
        )
      )
    }

  }

  private def hasAnyCorrespondenceDetails(userAnswers: UserAnswers): Boolean =
    userAnswers.get(CorrespondenceNamePage).isDefined ||
      userAnswers.get(CorrespondenceAdditionalNamePage).isDefined ||
      userAnswers.get(CorrespondenceAdditionalInformationPage).isDefined ||
      userAnswers.get(CorrespondenceFaxNumberPage).isDefined ||
      userAnswers.get(CorrespondenceEmailPage).isDefined ||
      userAnswers.get(isleMOrChannelFlagPage).isDefined ||
      userAnswers.get(CorrespondenceAddressUkPage).isDefined ||
      userAnswers.get(CorrespondenceAddressNonUkPage).isDefined ||
      userAnswers.get(CorrespondenceContactNumberPage).exists { contact =>
        contact.phoneNumber.isDefined ||
        contact.mobilePhoneNumber.isDefined
      }
}
