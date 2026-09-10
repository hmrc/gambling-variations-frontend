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

    "return true for a checksum-valid number that only passes the 9755 fallback" in {
      // weighted 112 + check 27 = 139; 139 % 97 = 42, but (139 + 55) % 97 = 0
      ChecksumValidator.isValidVatNumber("123456727") mustBe true
    }

    "return true for a checksum-valid number ignoring leading and trailing spaces" in {
      val formatted = Seq("   353868127", "353868127   ", "   353868127   ")
      for (input <- formatted)
        withClue(s"[$input] ") {
          ChecksumValidator.isValidVatNumber(input) mustBe true
        }
    }

    "return false for a well-formed number that fails the checksum" in {
      val checksumInvalid = Seq("000000002", "999999999", "353868121")
      for (input <- checksumInvalid)
        withClue(s"[$input] ") {
          ChecksumValidator.isValidVatNumber(input) mustBe false
        }
    }

    "return true for a boundary number" in {
      val quirkyVrn = "000000000"
      ChecksumValidator.isValidVatNumber(quirkyVrn) mustBe true
    }

    "return false when there are not exactly 9 digits" in {
      val incorrectLength = Seq("12345678", "1234567890", "0")
      for (input <- incorrectLength)
        withClue(s"[$input] ") {
          ChecksumValidator.isValidVatNumber(input) mustBe false
        }
    }

    "return false for a GB-prefixed number - GB is no longer supported" in {
      Seq("GB353868127", "gb353868127").foreach(
        ChecksumValidator.isValidVatNumber(_) mustBe false
      )
    }

    "return false when interspersed spaces and hyphens exist" in {
      val formatted = Seq("35 386812", "35868 2 7", "353-868127")
      for (input <- formatted)
        withClue(s"[$input] ") {
          ChecksumValidator.isValidVatNumber(input) mustBe false
        }
    }

    "return false for non-digit characters" in {
      ChecksumValidator.isValidVatNumber("3538X8127") mustBe false
    }

    "return false for empty or blank input" in {
      Seq("", "   ").foreach(ChecksumValidator.isValidVatNumber(_) mustBe false)
    }

    "return true for a checksum-valid number (from Confluence Spec)" in {
      val checksumValid =
        Seq("252525279", "127207295", "127207393", "239088635", "552778902", "123478768", "127207589", "127207491", "600002183", "562235945")
      for (input <- checksumValid)
        withClue(s"[$input] ") {
          ChecksumValidator.isValidVatNumber(input) mustBe true
        }
    }

  }

  "isValidUtr" - {

    "return true for a blank value" in {
      ChecksumValidator.isValidUtr("") mustBe true
      ChecksumValidator.isValidUtr("   ") mustBe true
    }

    "return true when the format and Modulo 11 checksum are valid" in {
      // Valid documented UTR sample
      ChecksumValidator.isValidUtr("1121766916") mustBe true
    }

    "return true when valid UTR contains spaces" in {
      ChecksumValidator.isValidUtr("112 176 6916") mustBe true
    }

    "return false when the value contains non-numeric characters" in {
      ChecksumValidator.isValidUtr("112176691A") mustBe false
      ChecksumValidator.isValidUtr("ABCDEFGHIJ") mustBe false
    }

    "return false when the length is less than 10 digits" in {
      ChecksumValidator.isValidUtr("112176691") mustBe false
    }

    "return false when the length is greater than 10 digits" in {
      ChecksumValidator.isValidUtr("11217669160") mustBe false
    }

    "return false when 10 digits are provided but the checksum calculation fails" in {
      ChecksumValidator.isValidUtr("1234567890") mustBe false
    }
  }
}
