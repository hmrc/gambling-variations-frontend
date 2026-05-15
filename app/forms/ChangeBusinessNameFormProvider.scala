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
    businessType match {
      case BusinessType.Soleproprietor              => "changeBusinessName.error.required.soleProprietor"
      case BusinessType.Partnership                 => "changeBusinessName.error.required.partnership"
      case BusinessType.Corporatebody               => "changeBusinessName.error.required.corporateBody"
      case BusinessType.Unincorporatedbody          => "changeBusinessName.error.required.unincorporatedBody"
      case BusinessType.LimitedLiabilityPartnership => "changeBusinessName.error.required.limitedLiabilityPartnership"
    }

  private def invalidErrorKeyFor(businessType: BusinessType): String =
    businessType match {
      case BusinessType.Soleproprietor              => "changeBusinessName.error.invalid.soleProprietor"
      case BusinessType.Partnership                 => "changeBusinessName.error.invalid.partnership"
      case BusinessType.Corporatebody               => "changeBusinessName.error.invalid.corporateBody"
      case BusinessType.Unincorporatedbody          => "changeBusinessName.error.invalid.unincorporatedBody"
      case BusinessType.LimitedLiabilityPartnership => "changeBusinessName.error.invalid.limitedLiabilityPartnership"
    }

  private def lengthErrorKeyFor(businessType: BusinessType): String =
    businessType match {
      case BusinessType.Soleproprietor              => "changeBusinessName.error.length.soleProprietor"
      case BusinessType.Partnership                 => "changeBusinessName.error.length.partnership"
      case BusinessType.Corporatebody               => "changeBusinessName.error.length.corporateBody"
      case BusinessType.Unincorporatedbody          => "changeBusinessName.error.length.unincorporatedBody"
      case BusinessType.LimitedLiabilityPartnership => "changeBusinessName.error.length.limitedLiabilityPartnership"
    }

  def apply(businessType: BusinessType): Form[String] = {

    val requiredKey = requiredErrorKeyFor(businessType)
    val invalidKey = invalidErrorKeyFor(businessType)
    val lengthKey = lengthErrorKeyFor(businessType)

    Form(
      "value" -> text(requiredKey)
        .verifying(maxLength(160, lengthKey))
        .verifying(regexp("""^[A-Za-z0-9' -]+$""", invalidKey))
    )
  }

}
