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

import config.FrontendAppConfig
import controllers.actions.*
import models.BusinessType
import pages.*
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.*
import views.html.ChangeRegistrationDetailsView

import javax.inject.Inject
import scala.concurrent.Future

class ChangeRegistrationDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  businessDetailsRequired: BusinessDetailsDataRequiredAction,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: ChangeRegistrationDetailsView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] =
    (authorise andThen getData andThen businessDetailsRequired).async { implicit request =>

      implicit val msgs: Messages = messagesApi.preferred(request)

      val result =
        request.userAnswers
          .get(GroupMemberPage)
          .map { isGroupMember =>

            val isPartnership =
              request.userAnswers
                .get(BusinessTypePage)
                .contains(BusinessType.Partnership)

            val businessNameChanged =
              request.userAnswers
                .get(BusinessNameChangesPage)
                .getOrElse(false)

            val businessAddressChanged =
              request.userAnswers
                .get(BusinessNameChangesPage)
                .getOrElse(false)

            val contactDetailsChanged =
              request.userAnswers
                .get(ContactDetailsChangesPage)
                .getOrElse(false)

            val correspondenceDetailsChanged =
              request.userAnswers
                .get(CorrespondenceDetailsChangesPage)
                .getOrElse(false)

            val tradingDetailsChanged =
              request.userAnswers
                .get(TradingDetailsChangesPage)
                .getOrElse(false)

            val licencesChanged = false
            val premisesExists = false
            val premisesTriggered = licencesChanged

            val submitUrl =
              routes.DeclarationController.onPageLoad().url

            val vm =
              ChangeRegistrationDetailsViewModel(
                mgdRegNumber                 = request.mgdRegNum,
                managementHomeUrl            = appConfig.gamblingManagementHomeUrl,
                isGroupMember                = isGroupMember,
                isPartnership                = isPartnership,
                businessNameChanged          = businessNameChanged,
                businessAddressChanged       = businessAddressChanged,
                contactDetailsChanged        = contactDetailsChanged,
                correspondenceDetailsChanged = correspondenceDetailsChanged,
                tradingDetailsChanged        = tradingDetailsChanged,
                licencesChanged              = licencesChanged,
                premisesExists               = premisesExists,
                premisesTriggered            = premisesTriggered,
                submitUrl                    = submitUrl
              )

            Ok(
              view(
                vm,
                request.mgdRegNum,
                appConfig.gamblingManagementHomeUrl,
                submitUrl
              )
            )
          }
          .getOrElse {
            logger.error(
              s"Missing GroupMemberPage for MGD registration number ${request.mgdRegNum}"
            )

            Redirect(routes.SystemErrorController.onPageLoad())
          }

      Future.successful(result)
    }
}
