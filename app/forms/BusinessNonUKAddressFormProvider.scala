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

import forms.mappings.Mappings
import models.Address
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

import javax.inject.Inject

class BusinessNonUKAddressFormProvider @Inject() extends Mappings {

  private val addressRegex =
    """^[A-Za-z0-9&'.,/\-\s]+$"""

  private val countryRegex =
    """^[A-Za-z'\-\s]+$"""

  def apply(): Form[Address] =
    Form(
      mapping(
        "addressLine1" -> text("businessNonUKAddress.addressLine1.error.required")
          .verifying(
            maxLength(
              27,
              "businessNonUKAddress.addressLine1.error.length"
            )
          )
          .verifying(
            regexp(
              addressRegex,
              "businessNonUKAddress.addressLine1.error.invalid"
            )
          ),
        "addressLine2" -> text("businessNonUKAddress.addressLine2.error.required")
          .verifying(
            maxLength(
              27,
              "businessNonUKAddress.addressLine2.error.length"
            )
          )
          .verifying(
            regexp(
              addressRegex,
              "businessNonUKAddress.addressLine2.error.invalid"
            )
          ),
        "townOrCity" -> text("businessNonUKAddress.townOrCity.error.required")
          .verifying(
            maxLength(
              27,
              "businessNonUKAddress.townOrCity.error.length"
            )
          )
          .verifying(
            regexp(
              addressRegex,
              "businessNonUKAddress.townOrCity.error.invalid"
            )
          ),
        "region" -> optional(text())
          .verifying(
            "businessNonUKAddress.Region.error.length",
            _.forall(_.length <= 18)
          )
          .verifying(
            "businessNonUKAddress.Region.error.invalid",
            _.forall(_.matches(addressRegex))
          ),
        "country" -> text("businessNonUKAddress.Country.error.required")
          .verifying(
            maxLength(
              27,
              "businessNonUKAddress.Country.error.length"
            )
          )
          .verifying(
            regexp(
              countryRegex,
              "businessNonUKAddress.Country.error.invalid"
            )
          )
      )((addressLine1, addressLine2, townOrCity, region, country) =>
        Address(
          address1 = addressLine1,
          address2 = Some(addressLine2),
          address3 = Some(townOrCity),
          address4 = region,
          postcode = None,
          country  = Some(country)
        )
      )(address =>
        Some(
          (
            address.address1,
            address.address2.getOrElse(""),
            address.address3.getOrElse(""),
            address.address4,
            address.country.getOrElse("")
          )
        )
      )
    )
}
