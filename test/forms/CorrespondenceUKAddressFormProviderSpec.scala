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

import forms.behaviours.StringFieldBehaviours
import models.Address
import play.api.data.FormError

class CorrespondenceUKAddressFormProviderSpec extends StringFieldBehaviours {

  private val form = new CorrespondenceUKAddressFormProvider()()

  private val addressRegex =
    """^[A-Za-z0-9&'.,/\-\s]+$"""

  private val postcodeCharactersRegex =
    """^[A-Za-z0-9\s]+$"""

  private val ukPostcodeRegex =
    """(?i)^([A-Z]{1,2}[0-9][A-Z0-9]?\s?[0-9][A-Z]{2})$"""

  private val validData: Map[String, String] =
    Map(
      "addressLine1" -> "10 Downing Street",
      "addressLine2" -> "Westminster",
      "townOrCity"   -> "London",
      "county"       -> "Greater London",
      "postcode"     -> "SW1A 2AA"
    )

  private def bindWith(fieldName: String, value: String) =
    form.bind(validData + (fieldName -> value))

  ".bind" - {

    "must bind valid data" in {

      val result = form.bind(validData)

      result.errors mustBe empty
      result.value.value mustEqual Address(
        address1 = "10 Downing Street",
        address2 = Some("Westminster"),
        address3 = Some("London"),
        address4 = Some("Greater London"),
        postcode = Some("SW1A 2AA"),
        country  = Some("GB")
      )
    }

    "must bind when county is empty because county is optional" in {

      val result = form.bind(validData + ("county" -> ""))

      result.errors mustBe empty
      result.value.value mustEqual Address(
        address1 = "10 Downing Street",
        address2 = Some("Westminster"),
        address3 = Some("London"),
        address4 = None,
        postcode = Some("SW1A 2AA"),
        country  = Some("GB")
      )
    }
  }

  ".addressLine1" - {

    val fieldName = "addressLine1"
    val requiredKey = "correspondenceUKAddress.addressLine1.error.required"
    val lengthKey = "correspondenceUKAddress.addressLine1.error.length"
    val invalidKey = "correspondenceUKAddress.addressLine1.error.invalid"
    val maxLength = 27

    "must not bind when empty" in {

      val result = bindWith(fieldName, "")

      result.errors must contain only FormError(fieldName, requiredKey)
    }

    "must not bind when longer than maximum length" in {

      val result = bindWith(fieldName, "A" * (maxLength + 1))

      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }

    "must not bind invalid characters" in {

      val invalidValues = Seq(
        "Test@",
        "Hello#World",
        "Invalid!",
        "Address$123"
      )

      invalidValues.foreach { value =>
        val result = bindWith(fieldName, value)

        result.errors must contain(
          FormError(fieldName, invalidKey, Seq(addressRegex))
        )
      }
    }
  }

  ".addressLine2" - {

    val fieldName = "addressLine2"
    val requiredKey = "correspondenceUKAddress.addressLine2.error.required"
    val lengthKey = "correspondenceUKAddress.addressLine2.error.length"
    val invalidKey = "correspondenceUKAddress.addressLine2.error.invalid"
    val maxLength = 27

    "must not bind when empty" in {

      val result = bindWith(fieldName, "")

      result.errors must contain only FormError(fieldName, requiredKey)
    }

    "must not bind when longer than maximum length" in {

      val result = bindWith(fieldName, "A" * (maxLength + 1))

      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }

    "must not bind invalid characters" in {

      val invalidValues = Seq(
        "Test@",
        "Hello#World",
        "Invalid!",
        "Address$123"
      )

      invalidValues.foreach { value =>
        val result = bindWith(fieldName, value)

        result.errors must contain(
          FormError(fieldName, invalidKey, Seq(addressRegex))
        )
      }
    }
  }

  ".townOrCity" - {

    val fieldName = "townOrCity"
    val requiredKey = "correspondenceUKAddress.townOrCity.error.required"
    val lengthKey = "correspondenceUKAddress.townOrCity.error.length"
    val invalidKey = "correspondenceUKAddress.townOrCity.error.invalid"
    val maxLength = 27

    "must not bind when empty" in {

      val result = bindWith(fieldName, "")

      result.errors must contain only FormError(fieldName, requiredKey)
    }

    "must not bind when longer than maximum length" in {

      val result = bindWith(fieldName, "A" * (maxLength + 1))

      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }

    "must not bind invalid characters" in {

      val invalidValues = Seq(
        "Town@",
        "City#",
        "Invalid!",
        "Town$123"
      )

      invalidValues.foreach { value =>
        val result = bindWith(fieldName, value)

        result.errors must contain(
          FormError(fieldName, invalidKey, Seq(addressRegex))
        )
      }
    }
  }

  ".county" - {

    val fieldName = "county"
    val lengthKey = "correspondenceUKAddress.County.error.length"
    val invalidKey = "correspondenceUKAddress.County.error.invalid"
    val maxLength = 18

    "must bind when empty" in {

      val result = bindWith(fieldName, "")

      result.errors mustBe empty
    }

    "must not bind when longer than maximum length" in {

      val result = bindWith(fieldName, "A" * (maxLength + 1))

      result.errors must contain(
        FormError(fieldName, lengthKey)
      )
    }

    "must not bind invalid characters" in {

      val invalidValues = Seq(
        "County@",
        "County#",
        "Invalid!",
        "County$123"
      )

      invalidValues.foreach { value =>
        val result = bindWith(fieldName, value)

        result.errors must contain(
          FormError(fieldName, invalidKey)
        )
      }
    }
  }

  ".postcode" - {

    val fieldName = "postcode"
    val requiredKey = "correspondenceUKAddress.Postcode.error.required"
    val invalidKey = "correspondenceUKAddress.Postcode.error.invalid"
    val formatKey = "correspondenceUKAddress.Postcode.error.format"
    val lengthKey = "correspondenceUKAddress.Postcode.error.length"
    val maxLength = 8

    "must not bind when empty" in {

      val result = bindWith(fieldName, "")

      result.errors must contain only FormError(fieldName, requiredKey)
    }

    "must not bind when longer than maximum length" in {

      val result = bindWith(fieldName, "SW1A 22AA")

      result.errors must contain(
        FormError(fieldName, lengthKey, Seq(maxLength))
      )
    }

    "must not bind invalid characters" in {

      val invalidValues = Seq(
        "SW1A-2AA",
        "SW1A@2AA",
        "SW1A#2AA",
        "SW1A/2AA"
      )

      invalidValues.foreach { value =>
        val result = bindWith(fieldName, value)

        result.errors must contain(
          FormError(fieldName, invalidKey, Seq(postcodeCharactersRegex))
        )
      }
    }

    "must not bind when postcode has valid characters but invalid UK format" in {

      val invalidValues = Seq(
        "ABC 123",
        "12345678",
        "ABCDEFGH",
        "A1 1AAA"
      )

      invalidValues.foreach { value =>
        val result = bindWith(fieldName, value)

        result.errors must contain(
          FormError(fieldName, formatKey, Seq(ukPostcodeRegex))
        )
      }
    }

    "must bind valid UK postcodes" in {

      val validPostcodes = Seq(
        "E1 6AN",
        "SW1A 2AA",
        "M1 1AE",
        "B33 8TH",
        "CR2 6XH",
        "DN55 1PT"
      )

      validPostcodes.foreach { value =>
        val result = bindWith(fieldName, value)

        result.errors mustBe empty
      }
    }
  }
}
