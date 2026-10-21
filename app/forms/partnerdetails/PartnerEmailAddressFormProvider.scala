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

package forms.partnerdetails

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.Constraint
import play.api.data.validation.Invalid
import play.api.data.validation.Valid

class PartnerEmailAddressFormProvider @Inject() extends Mappings {

  private val emailRegex =
    """^[A-Za-z0-9._-]{1,64}@[A-Za-z0-9._-]{1,64}$"""

  private def emailFormatConstraint(errorMessage: String): Constraint[String] =
    Constraint("constraints.emailFormat") { email =>
      val parts = email.split("@", -1)

      val valid =
        parts.length == 2 &&
          parts(0).nonEmpty &&
          parts(1).nonEmpty &&
          parts(0).length <= 64 &&
          parts(1).length <= 64 &&
          !parts(0).startsWith(".") &&
          !parts(0).endsWith(".") &&
          !parts(0).contains("..") &&
          parts(1).split("\\.", -1).forall { label =>
            label.nonEmpty &&
            label.head.isLetterOrDigit &&
            label.last.isLetterOrDigit &&
            label.forall(c => c.isLetterOrDigit || c == '-')
          } &&
          email.matches(emailRegex)

      if (valid) Valid
      else Invalid(errorMessage)
    }

  def apply(prefix: String): Form[String] =
    Form(
      "partnerEmailAddress" -> text(s"$prefix.error.required")
        .transform[String](_.trim, identity)
        .verifying(maxLength(70, s"$prefix.error.length"))
        .verifying(emailFormatConstraint(s"$prefix.error.invalid"))
    )
}
