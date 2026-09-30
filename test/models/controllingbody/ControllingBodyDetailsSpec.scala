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

class ControllingBodyDetailsSpec extends SpecBase {
  "ControllingBodyDetails" - {
    BusinessType.values.foreach { businessType =>
      s"read business type $businessType from the I1.36 field" in {
        val json = Json.obj("mgdRegNumber" -> mgdRegNum, "typeOfControllingBody" -> businessType.code, "businessName" -> "Name")
        val result = json.as[ControllingBodyDetails]
        result.typeOfControllingBody mustBe businessType
        result.businessName.value mustBe "Name"
      }
    }
    "use the controlling body's type independently of the registered business type" in {
      val json = Json.obj(
        "mgdRegNumber"          -> mgdRegNum,
        "typeOfControllingBody" -> 4,
        "businessType"          -> 2
      )
      json.as[ControllingBodyDetails].typeOfControllingBody mustBe BusinessType.Partnership
    }
    "not substitute businessType when the controlling body type is missing" in {
      Json.obj("mgdRegNumber" -> mgdRegNum, "businessType" -> 2).validate[ControllingBodyDetails].isError mustBe true
    }
    "read missing optional name fields" in {
      Json.obj("mgdRegNumber" -> mgdRegNum, "typeOfControllingBody" -> 4).as[ControllingBodyDetails] mustBe
        ControllingBodyDetails(mgdRegNum, BusinessType.Partnership)
    }
    "reject a missing business type" in {
      Json.obj("mgdRegNumber" -> mgdRegNum).validate[ControllingBodyDetails].isError mustBe true
    }
    "reject a null business type" in {
      Json.obj("mgdRegNumber" -> mgdRegNum, "typeOfControllingBody" -> play.api.libs.json.JsNull).validate[ControllingBodyDetails].isError mustBe true
    }
    "reject an invalid business type" in {
      Json.obj("mgdRegNumber" -> mgdRegNum, "typeOfControllingBody" -> 99).validate[ControllingBodyDetails].isError mustBe true
    }
    "reject a missing registration number" in {
      Json.obj("typeOfControllingBody" -> 4).validate[ControllingBodyDetails].isError mustBe true
    }
    "read sole proprietor fields without dropping middle names" in {
      val json = Json.obj(
        "mgdRegNumber"          -> mgdRegNum,
        "typeOfControllingBody" -> 1,
        "solePropTitle"         -> "Mr",
        "solePropFirstName"     -> "John",
        "solePropMiddleName"    -> "James",
        "solePropLastName"      -> "Smith"
      )
      json.as[ControllingBodyDetails].soleProprietorName.value mustBe models.SoleProprietorName("Mr", "John", Some("James"), "Smith")
    }
  }
}
