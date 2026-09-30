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
import models.{BusinessType, SoleProprietorName, UserAnswers}
import pages.controllingbody.{ControllingBodyBusinessNamePage, ControllingBodyBusinessTypePage, ControllingBodySoleProprietorPage}
import play.api.libs.json.{JsObject, JsString, Json}

class ControllingBodyCacheMigrationSpec extends SpecBase {
  private def read(data: JsObject): UserAnswers =
    Json.toJson(UserAnswers(mgdRegNum, data)).as[UserAnswers]

  "Reading cached controlling body details" - {
    BusinessType.values.foreach { businessType =>
      Seq(Json.toJson(businessType), JsString(businessType.toString)).foreach { storedType =>
        s"migrate legacy business type $storedType" in {
          val result = read(Json.obj("controllingBodyDetailsSection" -> Json.obj("typeOfControllingBody" -> storedType)))
          result.get(ControllingBodyBusinessTypePage).value mustBe businessType
          result.data mustBe Json.obj("controllingBodyDetails" -> Json.obj("businessType" -> businessType))
        }
      }
    }

    "preserve current edits and retain other legacy answers without restoring removed middle names" in {
      val editedName = SoleProprietorName("Ms", "Jane", None, "Jones")
      val result = read(
        Json.obj(
          "businessName" -> "Registered business",
          "controllingBodyDetailsSection" -> Json.obj(
            "typeOfControllingBody" -> "corporatebody",
            "businessName"          -> "Backend name",
            "soleProprietor"        -> SoleProprietorName("Mr", "Old", Some("Middle"), "Name"),
            "mgdRegNum"             -> mgdRegNum,
            "submitted"             -> true
          ),
          "controllingBodyDetails" -> Json.obj(
            "businessType"   -> BusinessType.Soleproprietor.code,
            "businessName"   -> "Edited name",
            "soleProprietor" -> editedName,
            "loaded"         -> true
          )
        )
      )
      result.get(ControllingBodyBusinessTypePage).value mustBe BusinessType.Soleproprietor
      result.get(ControllingBodyBusinessNamePage).value mustBe "Edited name"
      result.get(ControllingBodySoleProprietorPage).value mustBe editedName
      (result.data \ "businessName").as[String] mustBe "Registered business"
      (result.data \ "controllingBodyDetails" \ "submitted").as[Boolean] mustBe true
      (result.data \ "controllingBodyDetails" \ "mgdRegNum").as[String] mustBe mgdRegNum
      (result.data \ "controllingBodyDetails" \ "loaded").as[Boolean] mustBe true
      result.data.keys must not contain "controllingBodyDetailsSection"
      read(result.data).data mustBe result.data
    }

    "rename the old business-type key within the current section" in {
      val result = read(Json.obj("controllingBodyDetails" -> Json.obj("typeOfControllingBody" -> 4)))
      result.get(ControllingBodyBusinessTypePage).value mustBe BusinessType.Partnership
      result.data mustBe Json.obj("controllingBodyDetails" -> Json.obj("businessType" -> 4))
    }

    "leave unrelated session data unchanged" in {
      val data = Json.obj("businessName" -> "Registered business", "partners" -> Json.arr())
      read(data).data mustBe data
    }

    "preserve the full legacy section and session metadata through a read and write" in {
      val legacy = Json.obj(
        "mgdRegNum"                  -> mgdRegNum,
        "businessPartnerNumber"      -> "0100053091",
        "businessName"               -> "Original name",
        "tradingName"                -> "Trading name",
        "typeOfControllingBody"      -> "corporatebody",
        "soleProprietor"             -> SoleProprietorName("Ms", "Jane", Some("Mary"), "Jones"),
        "dateOfJoining"              -> "2020-01-01",
        "dateOfLeaving"              -> "2026-01-01",
        "dateOfBirth"                -> "1980-01-01",
        "dateOfIncorporation"        -> "2010-01-01",
        "countryOfIncorporation"     -> "GB",
        "foreignCorporateRef"        -> "REF123",
        "nino"                       -> "AB123456C",
        "utr"                        -> "1234567890",
        "vrn"                        -> "123456789",
        "crn"                        -> "12345678",
        "isRepMemSameAsCb"           -> "0",
        "isUkIncorporated"           -> "1",
        "submitted"                  -> true,
        "controllingBodyChangesPage" -> true,
        "correspondenceSection" -> Json.obj(
          "correspondenceAddress" -> Json.obj("address1" -> "1 Test Road", "postcode" -> "AA1 1AA"),
          "contactNumber"         -> Json.obj("phoneNumber" -> "01234567890"),
          "emailAddr"             -> "test@example.com"
        ),
        "additionalCachedField" -> Json.arr("retained", Json.obj("nested" -> true))
      )
      val original = UserAnswers(
        mgdRegNum,
        Json.obj(
          "controllingBodyDetailsSection" -> legacy,
          "partners"                      -> Json.obj("1" -> Json.obj("businessName" -> "Unrelated partner"))
        ),
        lastUpdated = java.time.Instant.parse("2026-09-30T10:00:00.123Z")
      )
      val migrated = Json.toJson(original).as[UserAnswers]
      val expectedData = Json.obj(
        "controllingBodyDetails" -> ((legacy - "typeOfControllingBody") + ("businessType" -> Json.toJson(BusinessType.Corporatebody.code))),
        "partners"               -> (original.data \ "partners").as[JsObject]
      )

      migrated mustBe original.copy(data = expectedData)
      Json.toJson(migrated).as[UserAnswers] mustBe migrated
    }

    "keep an unknown business type invalid rather than choosing a default" in {
      val result = read(Json.obj("controllingBodyDetailsSection" -> Json.obj("typeOfControllingBody" -> "unknown")))
      result.get(ControllingBodyBusinessTypePage) mustBe None
      (result.data \ "controllingBodyDetails" \ "businessType").as[String] mustBe "unknown"
    }
  }
}
