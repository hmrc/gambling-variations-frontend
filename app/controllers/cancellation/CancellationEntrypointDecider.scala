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

import models.{NormalMode, RegistrationStatus, UserAnswers}
import pages.RegistrationStatusPage
import pages.cancellation.RegistrationCancellationDeclarationPage
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

trait CancellationEntrypointDecider {

  enum CurrentScreen {
    case Cancel, Locked, Warning
  }

  def checkScreen(userAnswers: UserAnswers, currentScreen: CurrentScreen)(render: => Result): Result = {
    val validScreen: CurrentScreen = determineValidScreen(userAnswers)
    println("currentScreen: " + currentScreen)
    println("validScreen: " + validScreen)

    if (currentScreen == validScreen) {
      render
    } else {
      Redirect(validScreen match {
        case CurrentScreen.Cancel  => controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad(NormalMode)
        case CurrentScreen.Locked  => controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad()
        case CurrentScreen.Warning => controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad()
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
