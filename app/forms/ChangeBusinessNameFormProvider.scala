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
import models.BusinessType
import play.api.data.Form

class ChangeBusinessNameFormProvider @Inject() extends Mappings {
  
  private def requiredErrorKeyFor(businessType: BusinessType): String =
    s"changeBusinessName.error.required.${businessType.toString}"

  private def invalidErrorKeyFor(businessType: BusinessType): String =
    s"changeBusinessName.error.invalid.${businessType.toString}"

  private def lengthErrorKeyFor(businessType: BusinessType): String =
    s"changeBusinessName.error.length.${businessType.toString}"


  def apply(businessType: BusinessType): Form[String] = {

    val requiredKey = requiredErrorKeyFor(businessType)
    val invalidKey = invalidErrorKeyFor(businessType)
    val lengthKey = lengthErrorKeyFor(businessType)

    val (maxLen, regex) = businessType match {
      case BusinessType.Partnership =>
        (
          34,
          """^[A-Za-z0-9&'(),!\/ -]+$"""
        )

      case _ =>
        (
          160,
          """^[A-Za-z0-9' -]+$"""
        )
    }

    Form(
      "value" -> text(requiredKey)
        .transform[String](_.trim, identity)
        .verifying(requiredKey, _.nonEmpty)
        .verifying(maxLength(maxLen, lengthKey))
        .verifying(regexp(regex, invalidKey))
    )
  }


}
