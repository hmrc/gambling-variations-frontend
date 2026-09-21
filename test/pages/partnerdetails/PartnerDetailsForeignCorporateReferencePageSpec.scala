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

package pages.partnerdetails

//import org.scalatestplus.play.PlaySpec
//import play.api.libs.json.{JsPath, Json}
//
//class PartnerDetailsForeignCorporateReferencePageSpec extends PlaySpec {
//
//  private val Index = 0
//
//  "PartnerDetailsForeignCorporateReferencePage" must {
//
//    "have the correct path" in {
//      PartnerDetailsForeignCorporateReferencePage(Index).path mustEqual (JsPath \ "partners" \ Index \ "partnerDetailsForeignCorporateRef")
//    }
//
//    "have the correct toString value" in {
//
//      PartnerDetailsForeignCorporateReferencePage(Index).toString mustEqual "partnerDetailsForeignCorporateRef"
//    }
//
//    "be able to read and write PartnerDetailsForeignCorporateReferencePage values with correct index" in {
//
//      val value1 = "ForeignCorpRef-1"
//      val value2 = "ForeignCorpRef-2"
//
//      val json = Json.obj(
//        "partners" -> Json.arr(
//          Json.obj(
//            PartnerDetailsForeignCorporateReferencePage(Index).toString -> Json.toJson(value1)
//          ),
//          Json.obj(
//            PartnerDetailsForeignCorporateReferencePage(Index + 1).toString -> Json.toJson(value2)
//          )
//        )
//      )
//
//      PartnerDetailsForeignCorporateReferencePage(Index).path
//        .asSingleJson(json)
//        .validate[String]
//        .get mustEqual value1
//
//      PartnerDetailsForeignCorporateReferencePage(Index + 1).path
//        .asSingleJson(json)
//        .validate[String]
//        .get mustEqual value2
//    }
//
//  }
//
//  // TODO copy pasted from my merge,
//
//  "partners" must {
//    "PartnerDetailsForeignCorporateRefPage" must {
//
//      "have the correct path" in {
//        PartnerDetailsForeignCorporateRefPage(
//          businessNumber1
//        ).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsForeignCorporateRef")
//      }
//
//      "have the correct toString value" in {
//
//        PartnerDetailsForeignCorporateRefPage(businessNumber1).toString mustEqual "partnerDetailsForeignCorporateRef"
//      }
//
//      "be able to read and write PartnerDetailsForeignCorporateRefPage values with correct index" in {
//
//        val value1 = "Value1"
//        val value2 = "Value2"
//
//        val json = Json.obj(
//          "partners" -> Json.obj(
//            businessNumber1 -> Json.obj(
//              PartnerDetailsForeignCorporateRefPage(businessNumber1).toString -> Json.toJson(value1)
//            ),
//            businessNumber2 -> Json.obj(
//              PartnerDetailsForeignCorporateRefPage(businessNumber2).toString -> Json.toJson(value2)
//            )
//          )
//        )
//
//        PartnerDetailsForeignCorporateRefPage(businessNumber1).path
//          .asSingleJson(json)
//          .validate[String]
//          .get mustEqual value1
//
//        PartnerDetailsForeignCorporateRefPage(businessNumber2).path
//          .asSingleJson(json)
//          .validate[String]
//          .get mustEqual value2
//      }
//
//    }
//  }
//
//  "newPartners" must {
//    "PartnerDetailsForeignCorporateRefPage" must {
//
//      "have the correct path" in {
//        PartnerDetailsForeignCorporateRefPage(
//          newPartnersIndex1
//        ).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsForeignCorporateRef")
//      }
//
//      "have the correct toString value" in {
//
//        PartnerDetailsForeignCorporateRefPage(newPartnersIndex1).toString mustEqual "partnerDetailsForeignCorporateRef"
//      }
//
//      "be able to read and write PartnerDetailsForeignCorporateRefPage values with correct index" in {
//
//        val value1 = "Value1"
//        val value2 = "Value2"
//
//        val json = Json.obj(
//          "newPartners" -> Json.arr(
//            Json.obj(
//              PartnerDetailsForeignCorporateRefPage(newPartnersIndex1).toString -> Json.toJson(value1)
//            ),
//            Json.obj(
//              PartnerDetailsForeignCorporateRefPage(newPartnersIndex2).toString -> Json.toJson(value2)
//            )
//          )
//        )
//
//        PartnerDetailsForeignCorporateRefPage(newPartnersIndex1).path
//          .asSingleJson(json)
//          .validate[String]
//          .get mustEqual value1
//
//        PartnerDetailsForeignCorporateRefPage(newPartnersIndex2).path
//          .asSingleJson(json)
//          .validate[String]
//          .get mustEqual value2
//      }
//
//    }
//  }
//}
