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

//TODO is it worth it?
trait CancellationNavigator {

  // TODO name
  enum Cancel {
    case Proceed, Locked, Warning
  }

  def checkScreen(userAnswers: UserAnswers, screen: Cancel)(render: => Result): Result = {
    val a: Cancel = determineScreen(userAnswers)

    if (screen == a) {
      render
    } else {
      Redirect(a match {
        case Cancel.Proceed => controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad(NormalMode)
        case Cancel.Locked  => controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad()
        case Cancel.Warning => controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad()
      })
    }
  }

  private def determineScreen(userAnswers: UserAnswers): Cancel = {

    // User submitted cancellation, but if isAlreadyCancelled is false means HMRC didn't approve it yet and show warninig
    val registrationAlreadyCancelled = userAnswers.get(RegistrationCancellationDeclarationPage).contains(true)

    // HMRC already cancelled, has to go to locked
    val isAlreadyCancelled = userAnswers.get(RegistrationStatusPage).exists {
      case RegistrationStatus.Cancelled => registrationAlreadyCancelled
      case _                            => false
    }

    if (isAlreadyCancelled) {
      Cancel.Locked
    } else if (registrationAlreadyCancelled) {
      Cancel.Warning
    } else {
      Cancel.Proceed
    }
  }

}
