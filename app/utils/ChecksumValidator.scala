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

object ChecksumValidator {

  private val vatValidDigitsRegex: String = "^[1-9]+$"
  private val vatWeights = List(8, 7, 6, 5, 4, 3, 2, 0, 0)
  private val vatModulus = 97
  private val vatFallbackOffset = 55 // for newer VAT numbers.

  def isValidVatNumber(number: String): Boolean = {
    val vatDigits = number.strip().toUpperCase.stripPrefix("GB")
    val isWellFormed = vatDigits.length == 9 && vatDigits.matches(vatValidDigitsRegex)

    isWellFormed && {
      val digits = vatDigits.take(9).toList.map(_.asDigit)
      val weightedSum = vatWeights.zip(digits).map((w, d) => w * d).sum
      val checkDigits = digits(7) * 10 + digits(8)
      val checksum = weightedSum + checkDigits

      checksum % vatModulus == 0 || (checksum + vatFallbackOffset) % vatModulus == 0
    }
  }

  val mgdrnCharactersRegex: String = "^[A-Z0-9]+$"
  val mgdrnFormatRegex: String = "^X[A-HJ-NP-TV-Z]M[0]{4}[0-9]{7}$"
  val mgdrnChecksumWeights: IndexedSeq[Int] = IndexedSeq(0, 0, 9, 10, 11, 12, 13, 8, 7, 6, 5, 4, 3, 2)
  val mgdrnChecksumLookup: String = "ABCDEFGHXJKLMNYPQRSTZVW"
  val mgdrnCheckCharacterIndex: Int = 1

  def isValidMgdrn(value: String): Boolean =
    isValidModulo23(
      value,
      mgdrnFormatRegex,
      mgdrnChecksumWeights,
      mgdrnCheckCharacterIndex,
      mgdrnChecksumLookup
    )

  def isValidModulo23(
    value: String,
    formatRegex: String,
    weights: IndexedSeq[Int],
    checkCharacterIndex: Int,
    lookup: String
  ): Boolean = {
    if (value.isBlank) {
      true
    } else if (
      !value.matches(formatRegex) ||
      value.length != weights.length ||
      checkCharacterIndex < 0 ||
      checkCharacterIndex >= value.length ||
      lookup.length != 23
    ) {
      false
    } else {
      val total = value
        .zip(weights)
        .map { case (character, weight) =>
          val numericValue =
            if (character.isDigit) character - '0'
            else character - 'A' + 33

          numericValue * weight
        }
        .sum

      lookup(total % 23) == value(checkCharacterIndex)
    }
  }
}
