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

package controllers.partner

import play.api.libs.json.{JsObject, Json}

trait PartnerDetailsHelper {

  val index: Int = 0
  val mgdRegNumber: String = "XGM00000001761"
  val testFaxNumber: String = "0123456789"
  val testPhoneNumber: String = "0123456789"
  val testEmailAddress: String = "test@test.com"

  def cleanedData(faxNumber: Option[String] = None, phoneNumber: Option[String] = None, emailAddress: Option[String] = None): JsObject = Json.obj(
    "partners" -> Json.arr(
      Json.obj(
        "partnerDetailsMgdRegNumber" -> mgdRegNumber,
        "partnerDetailsBusinessName" -> "Partner1",
        "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
          "mgdRegNumber" -> mgdRegNumber,
          "correspondenceAddress" -> Json.obj(
            "address1" -> "Flat 1",
            "address2" -> "10 Market Road",
            "address3" -> "Felling",
            "address4" -> "Gateshead",
            "postcode" -> "NE8 1ZZ",
            "country"  -> "UK"
          ),
          "contactNumber" -> Json.obj(
            "phoneNumber"       -> phoneNumber,
            "mobilePhoneNumber" -> phoneNumber
          ),
          "faxNumber" -> faxNumber,
          "emailAddr" -> emailAddress
        )
      )
    )
  )

}
