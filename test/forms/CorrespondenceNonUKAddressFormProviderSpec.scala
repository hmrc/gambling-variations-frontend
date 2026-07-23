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

class CorrespondenceNonUKAddressFormProviderSpec extends StringFieldBehaviours {

  private val form = new CorrespondenceNonUKAddressFormProvider()()

  private val addressRegex =
    """^[A-Za-z0-9&'.,/\-\s]+$"""

  private val countryRegex =
    """^[A-Za-z'\-\s]+$"""

  private val validData: Map[String, String] =
    Map(
      "addressLine1" -> "10 Rue Example",
      "addressLine2" -> "Example District",
      "townOrCity"   -> "Paris",
      "region"       -> "75001",
      "country"      -> "France"
    )

  private def bindWith(fieldName: String, value: String) =
    form.bind(validData + (fieldName -> value))

  ".bind" - {

    "must bind valid data" in {

      val result = form.bind(validData)

      result.errors mustBe empty
      result.value.value mustEqual Address(
        address1 = "10 Rue Example",
        address2 = Some("Example District"),
        address3 = Some("Paris"),
        address4 = Some("75001"),
        postcode = None,
        country = Some("France")
      )
    }

    "must bind when region is empty because region is optional" in {

      val result = form.bind(validData + ("region" -> ""))

      result.errors mustBe empty
      result.value.value mustEqual Address(
        address1 = "10 Rue Example",
        address2 = Some("Example District"),
        address3 = Some("Paris"),
        address4 = None,
        postcode = None,
        country = Some("France")
      )
    }
  }

  ".addressLine1" - {

    val fieldName = "addressLine1"
    val requiredKey = "correspondenceNonUKAddress.addressLine1.error.required"
    val lengthKey = "correspondenceNonUKAddress.addressLine1.error.length"
    val invalidKey = "correspondenceNonUKAddress.addressLine1.error.invalid"
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
    val requiredKey = "correspondenceNonUKAddress.addressLine2.error.required"
    val lengthKey = "correspondenceNonUKAddress.addressLine2.error.length"
    val invalidKey = "correspondenceNonUKAddress.addressLine2.error.invalid"
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
    val requiredKey = "correspondenceNonUKAddress.townOrCity.error.required"
    val lengthKey = "correspondenceNonUKAddress.townOrCity.error.length"
    val invalidKey = "correspondenceNonUKAddress.townOrCity.error.invalid"
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

  ".region" - {

    val fieldName = "region"
    val lengthKey = "correspondenceNonUKAddress.Region.error.length"
    val invalidKey = "correspondenceNonUKAddress.Region.error.invalid"
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
        "Region@",
        "Region#",
        "Invalid!",
        "Region$123"
      )

      invalidValues.foreach { value =>
        val result = bindWith(fieldName, value)

        result.errors must contain(
          FormError(fieldName, invalidKey)
        )
      }
    }
  }

  ".country" - {

    val fieldName = "country"
    val requiredKey = "correspondenceNonUKAddress.Country.error.required"
    val lengthKey = "correspondenceNonUKAddress.Country.error.length"
    val invalidKey = "correspondenceNonUKAddress.Country.error.invalid"
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
        "France1",
        "Spain.",
        "Germany/",
        "Italy&",
        "Country@"
      )

      invalidValues.foreach { value =>
        val result = bindWith(fieldName, value)

        result.errors must contain(
          FormError(fieldName, invalidKey, Seq(countryRegex))
        )
      }
    }

    "must bind valid country names" in {

      val validValues = Seq(
        "France",
        "Spain",
        "United States",
        "Cote d Ivoire",
        "Timor-Leste",
        "People's Republic"
      )

      validValues.foreach { value =>
        val result = bindWith(fieldName, value)

        result.errors mustBe empty
      }
    }
  }
}
