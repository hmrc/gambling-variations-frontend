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
import connectors.GamblingConnector
import controllers.actions.*
import pages.GroupMemberPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.ChangeRegistrationDetailsViewModel
import views.html.ChangeRegistrationDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class ChangeRegistrationDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  businessDetailsRequired: BusinessDetailsDataRequiredAction,
  gamblingConnector: GamblingConnector,
  appConfig: FrontendAppConfig,
  val controllerComponents: MessagesControllerComponents,
  view: ChangeRegistrationDetailsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] =
    (authorise andThen getData andThen businessDetailsRequired).async { implicit request =>

      request.userAnswers
        .get(GroupMemberPage)
        .map { isGroupMember =>

          given HeaderCarrier =
            HeaderCarrierConverter.fromRequestAndSession(
              request,
              request.session
            )

          controllingBodyDetailsUrl(request.mgdRegNum)
            .map { controllingBodyUrl =>

              val vm =
                ChangeRegistrationDetailsViewModel.from(
                  userAnswers        = request.userAnswers,
                  mgdRegNumber       = request.mgdRegNum,
                  managementHomeUrl  = appConfig.gamblingManagementHomeUrl,
                  isGroupMember      = isGroupMember,
                  controllingBodyUrl = controllingBodyUrl
                )

              Ok(view(vm))
            }
            .recover { case NonFatal(e) =>
              logger.error(
                s"Unable to retrieve controlling body details for ${request.mgdRegNum}",
                e
              )
              Redirect(routes.SystemErrorController.onPageLoad())
            }
        }
        .getOrElse {
          logger.error(
            s"Missing GroupMemberPage for MGD registration number ${request.mgdRegNum}"
          )

          Future.successful(
            Redirect(routes.SystemErrorController.onPageLoad())
          )
        }
    }

  private def controllingBodyDetailsUrl(mgdRegNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {

    gamblingConnector.getControlBodyDetails(mgdRegNumber).map { details =>

      details.isRepMemSameAsCb match {
        case Some("1") =>
          controllers.controllingbody.routes.ControllingBodySameAsRepYesNoController.onPageLoad().url

        case Some("0") =>
          controllers.controllingbody.routes.ControllingBodyChangeScreenerController.onPageLoad().url

        case _ =>
          routes.PageNotFoundController.onPageLoad().url

      }
    }
  }
}
