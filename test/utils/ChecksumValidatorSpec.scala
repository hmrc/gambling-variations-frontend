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

package utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class ChecksumValidatorSpec extends AnyFreeSpec with Matchers {

  "isValidMgdrn" - {

    "return true for a blank value" in {
      ChecksumValidator.isValidMgdrn("") mustBe true
    }

    "return true when the format and checksum are valid" in {
      ChecksumValidator.isValidMgdrn("XRM00000000574") mustBe true
    }

    "return false when the format is invalid" in {
      ChecksumValidator.isValidMgdrn("XIM00000000574") mustBe false
    }

    "return false when the numeric section does not start with four zeroes" in {
      ChecksumValidator.isValidMgdrn("XRM12340000574") mustBe false
    }

    "return false when the checksum is invalid" in {
      ChecksumValidator.isValidMgdrn("XAM00001234567") mustBe false
    }

  }

  "isValidVatNumber" - {
    "return true for a checksum-valid number" in {
      val validVrn = "353868127"

      // weighted 167 + check 27 = 194; 194 % 97 = 0
      ChecksumValidator.isValidVatNumber(validVrn) mustBe true
    }

    "return true for a checksum-valid number when prefixed with valid GB" in {
      Seq("GB353868127", "gb353868127", "Gb353868127", "gB353868127")
        .foreach(ChecksumValidator.isValidVatNumber(_) mustBe true)
    }

    "return true for a checksum-valid number that only passes the 9755 fallback" in {
      // weighted 112 + check 27 = 139; 139 % 97 = 42, but (139 + 55) % 97 = 0
      ChecksumValidator.isValidVatNumber("123456727") mustBe true
    }

    "return true for a checksum-valid number ignoring leading and trailing spaces" in {
      val formatted = Seq("   GB353868127", "GB353868127   ", "   GB353868127   ")
      for (input <- formatted)
        withClue(s"[$input] ") {
          ChecksumValidator.isValidVatNumber(input) mustBe true
        }
    }

    "return false for a well-formed number that fails the checksum" in {
      ChecksumValidator.isValidVatNumber("353868121") mustBe false
    }

    "return false when there are not exactly 9 digits" in {
      val incorrectLength = Seq("12345678", "GB12345678", "1234567890", "GB1234567890", "GB", "GB0")
      for (input <- incorrectLength)
        withClue(s"[$input] ") {
          ChecksumValidator.isValidVatNumber(input) mustBe false
        }
    }

    "return false when spaces and hyphens exist" in {
      val formatted = Seq("GB35 3868127", "35386812 7", "GB353-868127")
      for (input <- formatted)
        withClue(s"[$input] ") {
          ChecksumValidator.isValidVatNumber(input) mustBe false
        }
    }

    "return false for non-digit characters" in {
      ChecksumValidator.isValidVatNumber("3538X8127") mustBe false
    }

    "return false for a non-GB prefix" in {
      ChecksumValidator.isValidVatNumber("XY353868127") mustBe false
    }

    "return false for empty or blank input" in {
      Seq("", "   ", "GB").foreach(ChecksumValidator.isValidVatNumber(_) mustBe false)
    }

    "return false for any zeros" in {
      Seq("000000000", "GB000000000", "123406727").foreach(ChecksumValidator.isValidVatNumber(_) mustBe false)
    }

  }
}
