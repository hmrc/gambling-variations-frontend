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

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class LicencesNumberFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "licencesNumber.error.required"
  private val invalidCharactersKey = "licencesNumber.error.invalidCharacters"
  private val invalidFormatKey = "licencesNumber.error.invalidFormat"

  private val formProvider = new LicencesNumberFormProvider()
  private val form = formProvider()

  ".gamblingLicenceNo" - {

    val fieldName = "gamblingLicenceNo"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      "123-456789-A-123456-789"
    )

    "bind valid licence numbers" in {
      val validLicenceNumbers = Seq(
        "123-456789-A-123456-789",
        "001-000001-Z-999999-001",
        "999-123456-B-654321-999"
      )

      validLicenceNumbers.foreach { licenceNumber =>
        val result = form.bind(Map(fieldName -> licenceNumber))

        result.value mustBe Some(licenceNumber)
      }
    }

    "allow spaces and strip them out" in {
      val result = form.bind(
        Map(fieldName -> "123 - 456789 - A - 123456 - 789")
      )

      result.value mustBe Some("123-456789-A-123456-789")
    }

    "reject invalid characters" in {
      val invalidLicenceNumbers = Seq(
        "123-456789-@-123456-789",
        "123-456789-_-123456-789",
        "123-456789-a-123456-789"
      )

      invalidLicenceNumbers.foreach { licenceNumber =>
        val result = form.bind(Map(fieldName -> licenceNumber))

        result.errors must contain(
          FormError(fieldName, invalidCharactersKey)
        )
      }
    }

    "reject invalid formats" in {
      val invalidLicenceNumbers = Seq(
        "12-456789-A-123456-789",
        "123-45678-A-123456-789",
        "123-456789-AA-123456-789",
        "123-456789-A-12345-789",
        "123-456789-A-123456-78",
        "123456789-A-123456-789",
        "123-456789-A123456-789"
      )

      invalidLicenceNumbers.foreach { licenceNumber =>
        val result = form.bind(Map(fieldName -> licenceNumber))

        result.errors must contain(
          FormError(fieldName, invalidFormatKey)
        )
      }
    }

    "reject licence numbers longer than 23 characters" in {
      val result = form.bind(
        Map(fieldName -> "123-456789-A-123456-7890")
      )

      result.errors.map(_.message) must contain(invalidFormatKey)
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
