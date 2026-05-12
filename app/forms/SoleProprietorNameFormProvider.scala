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

package forms

import forms.mappings.Mappings
import models.SoleProprietorName
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class SoleProprietorNameFormProvider @Inject() extends Mappings {

  private def optionalCleanText(max: Int, errorKey: String) =
    optional(
      text()
        .transform[String](_.trim, identity)
        .verifying(maxLength(max, errorKey))
    ).transform[Option[String]](
      _.filter(_.nonEmpty),
      identity
    )

  def apply(): Form[SoleProprietorName] =
    Form(
      mapping(
        "title" ->
          text("soleProprietorNameForm.error.title.required")
            .transform[String](_.trim, identity)
            .verifying(maxLength(100, "soleProprietorNameForm.error.title.length")),
        "firstName" ->
          text("soleProprietorNameForm.error.firstName.required")
            .transform[String](_.trim, identity)
            .verifying(maxLength(100, "soleProprietorNameForm.error.firstName.length")),
        "middleName" ->
          optionalCleanText(
            100,
            "soleProprietorNameForm.error.middleName.length"
          ),
        "lastName" ->
          text("soleProprietorNameForm.error.lastName.required")
            .transform[String](_.trim, identity)
            .verifying(maxLength(100, "soleProprietorNameForm.error.lastName.length"))
      )(SoleProprietorName.apply)(sp => Some((sp.title, sp.firstName, sp.middleName, sp.lastName)))
    )

}
