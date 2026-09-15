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

package forms.licencespremises

import forms.behaviours.CheckboxFieldBehaviours
import forms.licencespremises.OtherLicencesAndPermitsNIFormProvider
import models.UserAnswers
import models.licencespremises.OtherLicencesAndPermitsNI
import models.licencespremises.OtherLicencesAndPermitsNI.*
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.Helpers.stubMessages

class OtherLicencesAndPermitsNIFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new OtherLicencesAndPermitsNIFormProvider()()
  val userAnswers = UserAnswers(
    "id",
    Json.obj(
      "licencesPremisesSection" -> Json.obj(
        "mgdRegNum"    -> "XGM000001761",
        "amusement"    -> "1",
        "bingo"        -> "0",
        "bookmaking"   -> "0",
        "serveAlcohol" -> "1",
        "regCert"      -> "0"
      )
    )
  )

  ".permitsNI" - {

    val fieldName = "permitsNI"
    val requiredKey = "otherLicencesAndPermitsNI.error.required"

    behave like checkboxField[OtherLicencesAndPermitsNI](
      form,
      fieldName,
      validValues  = OtherLicencesAndPermitsNI.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
