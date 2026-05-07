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
import models.ApiBusinessType
import models.requests.DataRequest
import pages.*
import play.api.Logging
import viewmodels.*

import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.BusinessDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ChangeRegistrationDetailsView

import scala.concurrent.ExecutionContext

class ChangeRegistrationDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  appConfig: FrontendAppConfig,
  businessDetailsService: BusinessDetailsService,
  val controllerComponents: MessagesControllerComponents,
  view: ChangeRegistrationDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      //  Prefer mgdRefNum if available in your request
      val mgdRegNumber = request.mgdRegNum

      businessDetailsService
        .retrieveBusinessDetails(mgdRegNumber)
        .map { businessDetails =>

          val isGroupMember = businessDetails.groupReg
          val isPartnership =
            businessDetails.businessType.contains(ApiBusinessType.Partnership)

          val tasks = buildTaskList(isGroupMember, isPartnership)

          val canStart = tasks.exists(_.status == ReadyToSubmit)

          Ok(
            view(
              mgdRegNumber,
              appConfig.gamblingManagementHomeUrl,
              tasks,
              canStart
            )
          )
        }
        .recover { case ex =>
          logger.error(s"Failed to load business details for $mgdRegNumber", ex)
          Redirect(controllers.routes.SystemErrorController.onPageLoad())
        }
    }

  private def buildTaskList(
    isGroupMember: Boolean,
    isPartnership: Boolean
  )(implicit request: DataRequest[?], messages: Messages): Seq[TaskListItem] = {

    val businessNameChanged =
      request.userAnswers.get(BusinessNameChangesPage).getOrElse(false)

    val licencesChanged = false // will remove it once other pages available
    val premisesExists = false
    val premisesTriggered = licencesChanged

    def status(flag: Boolean): TaskStatus =
      if (flag) ReadyToSubmit else NoChange

    Seq(
      if (!isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.businessName"),
            routes.CheckBusinessNameController.onPageLoad().url,
            status(businessNameChanged)
          )
        )
      else None,
      if (!isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.businessAddress"),
            routes.IndexController.onPageLoad().url,
            NoChange
          )
        )
      else None,
      if (!isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.businessContactDetails"),
            routes.IndexController.onPageLoad().url,
            NoChange
          )
        )
      else None,
      Some(
        TaskListItem(
          messages("changeRegistrationDetails.correspondenceDetails"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      Some(
        TaskListItem(
          messages("changeRegistrationDetails.tradingDetails"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      Some(
        TaskListItem(
          messages("changeRegistrationDetails.returnPeriod"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      if (isPartnership)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.partnerDetails"),
            routes.IndexController.onPageLoad().url,
            NoChange
          )
        )
      else None,
      if (isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.groupMemberDetails"),
            "group-member-details",
            NoChange
          )
        )
      else None,
      if (isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.controllingBodyDetails"),
            "group-member-details",
            NoChange
          )
        )
      else None,
      if (isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.disbandMGDGroup"),
            "group-member-details",
            NoChange
          )
        )
      else None,
      if (!isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.premises"),
            routes.IndexController.onPageLoad().url,
            status(licencesChanged)
          )
        )
      else None,
      if (!isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.licences"),
            routes.IndexController.onPageLoad().url,
            status(licencesChanged)
          )
        )
      else None,
      if (!isGroupMember && premisesTriggered)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.premises"),
            "premises",
            if (premisesExists) NoChange else NotStarted
          )
        )
      else None
    ).flatten
  }
}
