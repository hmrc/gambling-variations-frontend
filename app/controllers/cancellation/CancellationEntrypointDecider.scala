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

package controllers.cancellation

import models.{Mode, RegistrationStatus, UserAnswers}
import pages.cancellation.RegistrationCancellationDeclarationPage
import pages.{GroupMemberPage, RegistrationStatusPage}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

trait CancellationEntrypointDecider {

  enum CurrentScreen {
    case Cancel, Locked, Warning
  }

  def checkScreen(userAnswers: UserAnswers, currentScreen: CurrentScreen, isGroupRoute: Boolean, mode: Mode)(render: => Result): Result = userAnswers
    .get(GroupMemberPage)
    .fold(Redirect(controllers.routes.SystemErrorController.onPageLoad().url)) { isGroup =>

      val validScreen: CurrentScreen = determineValidScreen(userAnswers)
      if (currentScreen == validScreen && isGroup == isGroupRoute) {
        render
      } else {
        Redirect(validScreen match {
          case CurrentScreen.Cancel =>
            if isGroup then controllers.cancellation.routes.CancelRegistrationDateController.onPageLoadGroup(mode)
            else controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad(mode)
          case CurrentScreen.Locked =>
            if isGroup then controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoadGroup()
            else controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad()
          case CurrentScreen.Warning =>
            if isGroup then controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoadGroup()
            else controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad()
        })
      }
    }

  private def determineValidScreen(userAnswers: UserAnswers): CurrentScreen = {
    val hmrcConfirmedCancellation = userAnswers.get(RegistrationStatusPage).exists {
      case RegistrationStatus.Cancelled => true
      case _                            => false
    }

    lazy val userHasCancelled = userAnswers.get(RegistrationCancellationDeclarationPage).contains(true)

    if (hmrcConfirmedCancellation) {
      CurrentScreen.Locked
    } else if (userHasCancelled) {
      CurrentScreen.Warning
    } else {
      CurrentScreen.Cancel
    }
  }

}
