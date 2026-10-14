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

package models.controllingbody

import base.SpecBase
import models.BusinessType
import play.api.libs.json.Json

class ControlBodyDetailsContractSpec extends SpecBase {
  "ControlBodyDetails" - {
    BusinessType.values.foreach { businessType =>
      s"read business type $businessType from the I1.36 field" in {
        val json = Json.obj("mgdRegNumber" -> mgdRegNum, "typeOfControllingBody" -> businessType.code, "businessName" -> "Name")
        val result = json.as[ControlBodyDetails]
        result.typeOfControllingBody.value mustBe businessType
        result.businessName.value mustBe "Name"
      }
    }
    "use the controlling body's type independently of the registered business type" in {
      val json = Json.obj(
        "mgdRegNumber"          -> mgdRegNum,
        "typeOfControllingBody" -> 4,
        "businessType"          -> 2
      )
      json.as[ControlBodyDetails].typeOfControllingBody.value mustBe BusinessType.Partnership
    }
    "allow an absent type without substituting the registered business type" in {
      val details = Json.obj("mgdRegNumber" -> mgdRegNum, "businessType" -> 2).as[ControlBodyDetails]
      details.typeOfControllingBody mustBe None
      details.businessName mustBe None
    }
    "allow a null controlling body type" in {
      Json
        .obj("mgdRegNumber" -> mgdRegNum, "typeOfControllingBody" -> play.api.libs.json.JsNull)
        .as[ControlBodyDetails]
        .typeOfControllingBody mustBe None
    }
    "reject an invalid business type" in {
      Json.obj("mgdRegNumber" -> mgdRegNum, "typeOfControllingBody" -> 99).validate[ControlBodyDetails].isError mustBe true
    }
    "reject a missing registration number" in {
      Json.obj("typeOfControllingBody" -> 4).validate[ControlBodyDetails].isError mustBe true
    }
  }
}
