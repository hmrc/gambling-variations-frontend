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
