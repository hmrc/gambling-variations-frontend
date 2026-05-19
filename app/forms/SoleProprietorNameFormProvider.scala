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

  private val titleRegex = "^[A-Za-z' -]+$"
  private val nameRegex = "^[A-Za-z0-9' -]+$"
  private val optionalNameRegex = "^$|^[A-Za-z0-9' -]+$"

  private def validateText(
    requiredKey: String,
    max: Int,
    lengthKey: String,
    invalidKey: String,
    regex: String
  ) =
    text(requiredKey)
      .transform[String](_.trim, identity)
      .verifying(maxLength(max, lengthKey))
      .verifying(regexp(regex, invalidKey))

  private def optionalValidateText(max: Int, lengthKey: String, invalidKey: String) =
    optional(
      text()
        .transform[String](_.trim, identity)
        .verifying(maxLength(max, lengthKey))
        .verifying(regexp(optionalNameRegex, invalidKey))
    ).transform[Option[String]](
      _.filter(_.nonEmpty),
      identity
    )

  def apply(): Form[SoleProprietorName] =
    Form(
      mapping(
        "title" ->
          validateText(
            "soleProprietorName.error.title.required",
            20,
            "soleProprietorName.error.title.length",
            "soleProprietorName.error.title.invalid",
            titleRegex
          ),
        "firstName" ->
          validateText(
            "soleProprietorName.error.firstName.required",
            100,
            "soleProprietorName.error.firstName.length",
            "soleProprietorName.error.firstName.invalid",
            nameRegex
          ),
        "middleName" ->
          optionalValidateText(
            100,
            "soleProprietorName.error.middleName.length",
            "soleProprietorName.error.middleName.invalid"
          ),
        "lastName" ->
          validateText(
            "soleProprietorName.error.lastName.required",
            100,
            "soleProprietorName.error.lastName.length",
            "soleProprietorName.error.lastName.invalid",
            nameRegex
          )
      )(SoleProprietorName.apply)(sp => Some((sp.title, sp.firstName, sp.middleName, sp.lastName)))
    )

}
