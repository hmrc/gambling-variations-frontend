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

package models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, JsString, Json}

import java.time.LocalDate

class GamblingReturnPeriodsSpec extends AnyWordSpec with Matchers {

  import GamblingReturnPeriodsSpec.*

  "GamblingReturnPeriods JSON format" should {

    "serialize all fields correctly" in {
      val json = Json.toJson(sampleGamblingReturnPeriods)

      (json \ "mgdRegNumber").as[String] mustBe "XYZ00000000001"
      (json \ "returnPeriodsId").as[Int] mustBe 1

      (json \ "nstpEndDate1").as[String] mustBe "31-Mar-26"
      (json \ "nstpEndDate2").as[String] mustBe "30-Jun-26"

      (json \ "isInLastNstp").as[String] mustBe "1"
      (json \ "finalPeriodWarning").as[String] mustBe "0"
      (json \ "hasExistingNstpValues").as[String] mustBe "1"
    }

    "deserialize JSON correctly" in {
      val json = Json.toJson(sampleGamblingReturnPeriods)

      val result = json.as[GamblingReturnPeriods]

      result mustBe sampleGamblingReturnPeriods
    }

    "format LocalDate fields using the expected date format" in {
      val json = Json.toJson(sampleGamblingReturnPeriods)

      (json \ "nstpEndDate1").as[String] mustBe "31-Mar-26"
      (json \ "nstpEndDate2").as[String] mustBe "30-Jun-26"
    }

    "deserialize LocalDate fields using the expected date format" in {
      val json = Json.obj(
        "mgdRegNumber"          -> "XYZ00000000001",
        "returnPeriodsId"       -> 1,
        "nstpEndDate1"          -> "31-Mar-26",
        "nstpEndDate2"          -> "30-Jun-26",
        "nstpEndDate3"          -> JsString("30-Sep-26"),
        "nstpEndDate4"          -> JsString("31-Dec-26"),
        "nstpEndDate5"          -> JsString("31-Mar-27"),
        "nstpEndDate6"          -> JsString("30-Jun-27"),
        "nstpEndDate7"          -> JsString("30-Sep-27"),
        "nstpEndDate8"          -> JsString("31-Dec-27"),
        "isInLastNstp"          -> "1",
        "finalPeriodWarning"    -> "0",
        "hasExistingNstpValues" -> "1"
      )

      val result = json.as[GamblingReturnPeriods]

      result.nstpEndDate1 mustBe Some(LocalDate.of(2026, 3, 31))
      result.nstpEndDate2 mustBe Some(LocalDate.of(2026, 6, 30))
      result.nstpEndDate3 mustBe Some(LocalDate.of(2026, 9, 30))
      result.nstpEndDate4 mustBe Some(LocalDate.of(2026, 12, 31))
      result.nstpEndDate8 mustBe Some(LocalDate.of(2027, 12, 31))
    }

    "serialize boolean fields as string values 1 and 0" in {
      val trueJson = Json.toJson(sampleGamblingReturnPeriods)
      (trueJson \ "isInLastNstp").as[String] mustBe "1"
      (trueJson \ "hasExistingNstpValues").as[String] mustBe "1"

      val falseJson = Json.toJson(
        sampleGamblingReturnPeriods.copy(
          isInLastNstp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNstpValues = Some(false)
        )
      )

      (falseJson \ "isInLastNstp").as[String] mustBe "0"
      (falseJson \ "finalPeriodWarning").as[String] mustBe "0"
      (falseJson \ "hasExistingNstpValues").as[String] mustBe "0"
    }

    "deserialize boolean fields from string values 1 and 0" in {
      val json = Json.obj(
        "mgdRegNumber"          -> "XYZ00000000001",
        "returnPeriodsId"       -> 1,
        "nstpEndDate1"          -> JsString("31-Mar-26"),
        "nstpEndDate2"          -> JsString("30-Jun-26"),
        "nstpEndDate3"          -> JsString("30-Sep-26"),
        "nstpEndDate4"          -> JsString("31-Dec-26"),
        "nstpEndDate5"          -> JsString("31-Mar-27"),
        "nstpEndDate6"          -> JsString("30-Jun-27"),
        "nstpEndDate7"          -> JsString("30-Sep-27"),
        "nstpEndDate8"          -> JsString("31-Dec-27"),
        "isInLastNstp"          -> "1",
        "finalPeriodWarning"    -> "0",
        "hasExistingNstpValues" -> "1"
      )

      val result = json.as[GamblingReturnPeriods]

      result.isInLastNstp mustBe Some(true)
      result.finalPeriodWarning mustBe Some(false)
      result.hasExistingNstpValues mustBe Some(true)
    }

    "handle empty optional fields" in {
      val minimal = GamblingReturnPeriods(
        mgdRegNumber          = "XYZ00000000001",
        returnPeriodsId       = None,
        nstpEndDate1          = None,
        nstpEndDate2          = None,
        nstpEndDate3          = None,
        nstpEndDate4          = None,
        nstpEndDate5          = None,
        nstpEndDate6          = None,
        nstpEndDate7          = None,
        nstpEndDate8          = None,
        isInLastNstp          = None,
        finalPeriodWarning    = None,
        hasExistingNstpValues = None
      )

      val json = Json.toJson(minimal)
      val result = json.as[GamblingReturnPeriods]

      result mustBe minimal
    }

    "handle null optional fields" in {
      val json = Json.obj(
        "mgdRegNumber"          -> "XYZ00000000001",
        "returnPeriodsId"       -> 1,
        "nstpEndDate1"          -> JsString("31-Mar-26"),
        "nstpEndDate2"          -> play.api.libs.json.JsNull,
        "nstpEndDate3"          -> play.api.libs.json.JsNull,
        "nstpEndDate4"          -> play.api.libs.json.JsNull,
        "nstpEndDate5"          -> play.api.libs.json.JsNull,
        "nstpEndDate6"          -> play.api.libs.json.JsNull,
        "nstpEndDate7"          -> play.api.libs.json.JsNull,
        "nstpEndDate8"          -> play.api.libs.json.JsNull,
        "isInLastNstp"          -> play.api.libs.json.JsNull,
        "finalPeriodWarning"    -> play.api.libs.json.JsNull,
        "hasExistingNstpValues" -> play.api.libs.json.JsNull
      )

      val result = json.as[GamblingReturnPeriods]

      result.nstpEndDate1 mustBe Some(LocalDate.of(2026, 3, 31))
      result.nstpEndDate2 mustBe None
      result.isInLastNstp mustBe None
      result.finalPeriodWarning mustBe None
      result.hasExistingNstpValues mustBe None
    }

    "reject invalid boolean string values" in {
      val result = GamblingReturnPeriods.stringToBoolean.reads(JsString("2"))

      result mustBe a[JsError]
    }

    "reject invalid date formats" in {
      val json = Json.obj(
        "mgdRegNumber"          -> "XYZ00000000001",
        "returnPeriodsId"       -> 1,
        "nstpEndDate1"          -> "2026-03-31",
        "nstpEndDate2"          -> JsString("30-Jun-26"),
        "nstpEndDate3"          -> JsString("30-Sep-26"),
        "nstpEndDate4"          -> JsString("31-Dec-26"),
        "nstpEndDate5"          -> JsString("31-Mar-27"),
        "nstpEndDate6"          -> JsString("30-Jun-27"),
        "nstpEndDate7"          -> JsString("30-Sep-27"),
        "nstpEndDate8"          -> JsString("31-Dec-27"),
        "isInLastNstp"          -> "1",
        "finalPeriodWarning"    -> "0",
        "hasExistingNstpValues" -> "1"
      )

      json.validate[GamblingReturnPeriods] mustBe a[JsError]
    }

    "round-trip JSON without changing the object" in {
      val json = Json.toJson(sampleGamblingReturnPeriods)
      val result = json.as[GamblingReturnPeriods]

      result mustBe sampleGamblingReturnPeriods
    }
  }
}

object GamblingReturnPeriodsSpec {

  private val sampleGamblingReturnPeriods = GamblingReturnPeriods(
    mgdRegNumber          = "XYZ00000000001",
    returnPeriodsId       = Some(1),
    nstpEndDate1          = Some(LocalDate.of(2026, 3, 31)),
    nstpEndDate2          = Some(LocalDate.of(2026, 6, 30)),
    nstpEndDate3          = Some(LocalDate.of(2026, 9, 30)),
    nstpEndDate4          = Some(LocalDate.of(2026, 12, 31)),
    nstpEndDate5          = Some(LocalDate.of(2027, 3, 31)),
    nstpEndDate6          = Some(LocalDate.of(2027, 6, 30)),
    nstpEndDate7          = Some(LocalDate.of(2027, 9, 30)),
    nstpEndDate8          = Some(LocalDate.of(2027, 12, 31)),
    isInLastNstp          = Some(true),
    finalPeriodWarning    = Some(false),
    hasExistingNstpValues = Some(true)
  )
}
