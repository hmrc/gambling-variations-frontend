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

package forms.controllingbody

import forms.controllingbody.ControllingBodyForeignCorpRefFormProvider.*
import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class ControllingBodyForeignCorpRefFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" ->
        text(requiredKey)
          .transform[String](
            value => value.trim.replaceAll("(?<=\\d)\\s{2,}(?=\\d)", " "),
            identity
          )
          .verifying(
            regexp(
              refNumberRegex,
              invalidKey
            )
          )
          .verifying(maxLength(100, maxLengthKey))
    )
}

object ControllingBodyForeignCorpRefFormProvider {

  private[forms] val maxLength = 100
  private[forms] val refNumberRegex = "^[A-Za-z 0-9-']+$"
  private[forms] val requiredKey = "controllingBodyForeignCorpRef.error.required"
  private[forms] val maxLengthKey = "controllingBodyForeignCorpRef.error.length"
  private[forms] val invalidKey = "controllingBodyForeignCorpRef.error.invalid"
}
