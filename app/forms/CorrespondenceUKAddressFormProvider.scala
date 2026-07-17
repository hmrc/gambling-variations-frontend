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

class CorrespondenceUKAddressFormProvider @Inject() extends Mappings {

  private val addressRegex =
    """^[A-Za-z0-9&'.,/\-\s]+$"""

  private val postcodeCharactersRegex =
    """^[A-Za-z0-9\s]+$"""

  private val ukPostcodeRegex =
    """(?i)^([A-Z]{1,2}[0-9][A-Z0-9]?\s?[0-9][A-Z]{2})$"""

  def apply(): Form[Address] =
    Form(
      mapping(
        "addressLine1" -> text("correspondenceUKAddress.addressLine1.error.required")
          .verifying(
            maxLength(
              27,
              "correspondenceUKAddress.addressLine1.error.length"
            )
          )
          .verifying(
            regexp(
              addressRegex,
              "correspondenceUKAddress.addressLine1.error.invalid"
            )
          ),

        "addressLine2" -> text("correspondenceUKAddress.addressLine2.error.required")
          .verifying(
            maxLength(
              27,
              "correspondenceUKAddress.addressLine2.error.length"
            )
          )
          .verifying(
            regexp(
              addressRegex,
              "correspondenceUKAddress.addressLine2.error.invalid"
            )
          ),

        "townOrCity" -> text("correspondenceUKAddress.townOrCity.error.required")
          .verifying(
            maxLength(
              27,
              "correspondenceUKAddress.townOrCity.error.length"
            )
          )
          .verifying(
            regexp(
              addressRegex,
              "correspondenceUKAddress.townOrCity.error.invalid"
            )
          ),

        "county" -> optional(text())
          .verifying(
            "correspondenceUKAddress.County.error.length",
            _.forall(_.length <= 18)
          )
          .verifying(
            "correspondenceUKAddress.County.error.invalid",
            _.forall(_.matches(addressRegex))
          ),

        "postcode" -> text("correspondenceUKAddress.Postcode.error.required")
          .verifying(
            maxLength(
              8,
              "correspondenceUKAddress.Postcode.error.length"
            )
          )
          .verifying(
            regexp(
              postcodeCharactersRegex,
              "correspondenceUKAddress.Postcode.error.invalid"
            )
          )
          .verifying(
            regexp(
              ukPostcodeRegex,
              "correspondenceUKAddress.Postcode.error.format"
            )
          )
      )(
        (addressLine1, addressLine2, townOrCity, county, postcode) =>
          Address(
            address1 = addressLine1,
            address2 = Some(addressLine2),
            address3 = Some(townOrCity),
            address4 = county,
            postcode = Some(postcode),
            country = Some("GB")
          )
      )(
        address =>
          Some(
            (
              address.address1,
              address.address2.getOrElse(""),
              address.address3.getOrElse(""),
              address.address4,
              address.postcode.getOrElse("")
            )
          )
      )
    )
}
