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
import models.requests.DataRequest
import pages.*
import viewmodels.*

import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ChangeRegistrationDetailsView

class ChangeRegistrationDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: ChangeRegistrationDetailsView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      val mgdRegNumber = request.userId

      val tasks = buildTaskList()

      val canStart = tasks.exists(_.status == ReadyToSubmit)

      Ok(view(mgdRegNumber, appConfig.gamblingManagementHomeUrl, tasks, canStart))
    }

  private def buildTaskList()(implicit request: DataRequest[?], messages: Messages): Seq[TaskListItem] = {

    val isGroupMember = false // TODO derive properly

    val businessType =
      request.userAnswers.get(BusinessTypePage).map(_.toString).getOrElse("")

    val businessNameChanged =
      request.userAnswers.get(BusinessNameChangesPage).getOrElse(false)

    val licencesChanged = false
    val premisesExists = false
    val premisesTriggered = licencesChanged

    def status(flag: Boolean): TaskStatus =
      if (flag) ReadyToSubmit else NoChange

    Seq(
      if (!isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.businessName"),
            routes.BusinessNameController.onPageLoad().url,
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
      if (businessType == "partnership")
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
