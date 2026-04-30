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

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.SoleProprietorNameForm

class SoleProprietorNameFormFormProvider @Inject() extends Mappings {

   def apply(): Form[SoleProprietorNameForm] = Form(
     mapping(
      "title" -> text("soleProprietorNameForm.error.title.required")
        .verifying(maxLength(100, "soleProprietorNameForm.error.title.length")),
      "firstName" -> text("soleProprietorNameForm.error.firstName.required")
        .verifying(maxLength(100, "soleProprietorNameForm.error.firstName.length"),
      "middleName" -> text("soleProprietorNameForm.error.middleName.required")
        .verifying(maxLength(100, "soleProprietorNameForm.error.middleName.length"),
        "lastName" -> text("soleProprietorNameForm.error.lastName.required")
        .verifying(maxLength(100, "soleProprietorNameForm.error.firstName.length"))
    )((title: String, firstName: String) => SoleProprietorNameForm.apply(title, firstName,middleName,lastName))
    (x => Some((x.title, x.firstName)))
   )
 }
