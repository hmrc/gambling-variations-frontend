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

import forms.behaviours.StringFieldBehaviours
import forms.partnerdetails.PartnerDetailsCompanyRegistrationNumberFormProvider.*
import org.scalacheck.Gen
import play.api.data.FormError

class PartnerDetailsCompanyRegistrationNumberFormProviderSpec extends StringFieldBehaviours {

  private val form = new PartnerDetailsCompanyRegistrationNumberFormProvider()()

  // One 8-character example per branch of the Confluence regex
  private val validCrns = Seq(
    "12345678",
    "AC123456",
    "SC123456",
    "NI123456",
    "RO123456",
    "IP123456",
    "CU123456",
    "12345678", // 8 digits
    "00012345", // hint example
    "SC123456", // AC FC IC OC RC SC ZC + 6 digits
    "OC303030",
    "ZC000009",
    "CU123456", // CU + 6 digits
    "SA123456", // SA SE SF SI SR SZ + 6 digits
    "SZ001234",
    "ES123456", // ES + 6 digits
    "IP12345A", // IP SP + 6 letters/digits
    "SP0001AB",
    "NI123456", // NA NF NI NO NP NR NV NZ + 6 letters/digits
    "NPABCDEF",
    "RO123ABC" // RO + 6 letters/digits
  )

  ".value" - {

    val fieldName = "value"

    val invalidCharactersError = FormError(fieldName, invalidCharactersKey, Seq(allowedCharsRegex))
    val invalidFormatError = FormError(fieldName, invalidFormatKey, Seq(crnRegex))

    def errorsFor(value: String): Seq[FormError] = form.bind(Map(fieldName -> value)).errors

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(validCrns)
    )

    "bind every valid CRN format with no errors" in {
      validCrns.foreach { crn =>
        withClue(s"$crn: ") {
          val result = form.bind(Map(fieldName -> crn))
          result.errors mustBe empty
          result.value.value mustBe crn
        }
      }
    }

    "bind a CRN with a lowercase prefix" in {
      Seq("sc123456", "np123456", "ro123abc").foreach { crn =>
        withClue(s"$crn: ")(errorsFor(crn) mustBe empty)
      }
    }

    "bind a valid CRN ignoring leading and trailing spaces" in {
      Seq("   SC123456", "SC123456   ", "   SC123456   ").foreach { input =>
        val result = form.bind(Map(fieldName -> input))
        result.errors mustBe empty
        result.value.value mustBe "SC123456"
      }
    }

    "fail to bind the Confluence incorrect-format example ZZ345678" in {
      errorsFor("ZZ345678") mustBe Seq(invalidFormatError)
    }

    "fail to bind the Confluence invalid-characters example C@345678, with the characters and format errors" in {
      errorsFor("C@345678") mustBe Seq(invalidCharactersError, invalidFormatError)
    }

    "fail to bind 8 letters without a valid prefix" in {
      Seq("dddddddd", "ABCDEFGH").foreach { crn =>
        withClue(s"$crn: ")(errorsFor(crn) mustBe Seq(invalidFormatError))
      }
    }

    "fail to bind a prefix that Companies House does not use" in {
      Seq("AB123456", "XM123456", "NX123456").foreach { crn =>
        withClue(s"$crn: ")(errorsFor(crn) mustBe Seq(invalidFormatError))
      }
    }

    "fail to bind letters after a prefix that only allows digits" in {
      Seq("SC12A456", "CU12345A", "ES12345A").foreach { crn =>
        withClue(s"$crn: ")(errorsFor(crn) mustBe Seq(invalidFormatError))
      }
    }

    "fail to bind spaces, hyphens or apostrophes inside the CRN, with the characters and format errors" in {
      Seq("SC 12345", "SC-12345", "SC'12345").foreach { crn =>
        withClue(s"$crn: ")(errorsFor(crn) mustBe Seq(invalidCharactersError, invalidFormatError))
      }
    }

    "fail to bind other invalid characters, with the characters and format errors" in {
      Seq("SC12@456", "12345#78", "NP12!456").foreach { crn =>
        withClue(s"$crn: ")(errorsFor(crn) mustBe Seq(invalidCharactersError, invalidFormatError))
      }
    }

    "report every error, in order, for a value that breaks all three rules" in {
      errorsFor("SC@1") mustBe Seq(invalidCharactersError, invalidFormatError)
    }
  }
}
