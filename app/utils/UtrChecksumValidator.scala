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

object UtrChecksumValidator {

  val utrFormatRegex: String = "^[0-9]{10}$"
  val utrChecksumWeights: IndexedSeq[Int] = IndexedSeq(6, 7, 8, 9, 10, 5, 4, 3, 2)
  val utrCheckCharacterIndex: Int = 0

  def isValidUtr(value: String): Boolean = {
    val cleanedValue = value.replaceAll("\\s", "")

    if (cleanedValue.isBlank) {
      true
    } else if (!cleanedValue.matches(utrFormatRegex) || cleanedValue.length != 10) {
      false
    } else {
      val expectedCheckDigit = cleanedValue(utrCheckCharacterIndex) - '0'
      val payload = cleanedValue.substring(1)

      val total = payload
        .zip(utrChecksumWeights)
        .map { case (character, weight) =>
          (character - '0') * weight
        }
        .sum

      val modResult = total % 11
      val rawChecksum = 11 - modResult
      val calculatedChecksum = if (rawChecksum > 9) rawChecksum - 9 else rawChecksum

      calculatedChecksum == expectedCheckDigit
    }
  }
}
