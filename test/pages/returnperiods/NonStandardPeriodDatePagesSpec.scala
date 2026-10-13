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

package pages.returnperiods

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsPath, Json}

import java.time.LocalDate

class NonStandardPeriodDatePagesSpec extends AnyFreeSpec with Matchers {

  "NonStandardPeriodDate1Page" - {
    "must have the correct toString value" in {
      NonStandardPeriodDate1Page.toString mustBe "NonStandardPeriodDate1Page"
    }

    "must have the correct path" in {
      NonStandardPeriodDate1Page.path mustBe (JsPath \ "nstpEndDate1")
    }

    "must read and write LocalDate values correctly" in {
      val date = LocalDate.of(2026, 1, 1)
      val json = Json.obj("nstpEndDate1" -> Json.toJson(date))

      NonStandardPeriodDate1Page.path
        .asSingleJson(json)
        .validate[LocalDate]
        .get mustBe date
    }
  }

  "NonStandardPeriodDate2Page" - {
    "must have the correct toString value" in {
      NonStandardPeriodDate2Page.toString mustBe "NonStandardPeriodDate2Page"
    }

    "must have the correct path" in {
      NonStandardPeriodDate2Page.path mustBe (JsPath \ "nstpEndDate2")
    }

    "must read and write LocalDate values correctly" in {
      val date = LocalDate.of(2026, 2, 1)
      val json = Json.obj("nstpEndDate2" -> Json.toJson(date))

      NonStandardPeriodDate2Page.path
        .asSingleJson(json)
        .validate[LocalDate]
        .get mustBe date
    }
  }

  "NonStandardPeriodDate3Page" - {
    "must have the correct toString value" in {
      NonStandardPeriodDate3Page.toString mustBe "NonStandardPeriodDate3Page"
    }

    "must have the correct path" in {
      NonStandardPeriodDate3Page.path mustBe (JsPath \ "nstpEndDate3")
    }

    "must read and write LocalDate values correctly" in {
      val date = LocalDate.of(2026, 3, 1)
      val json = Json.obj("nstpEndDate3" -> Json.toJson(date))

      NonStandardPeriodDate3Page.path
        .asSingleJson(json)
        .validate[LocalDate]
        .get mustBe date
    }
  }

  "NonStandardPeriodDate4Page" - {
    "must have the correct toString value" in {
      NonStandardPeriodDate4Page.toString mustBe "NonStandardPeriodDate4Page"
    }

    "must have the correct path" in {
      NonStandardPeriodDate4Page.path mustBe (JsPath \ "nstpEndDate4")
    }

    "must read and write LocalDate values correctly" in {
      val date = LocalDate.of(2026, 4, 1)
      val json = Json.obj("nstpEndDate4" -> Json.toJson(date))

      NonStandardPeriodDate4Page.path
        .asSingleJson(json)
        .validate[LocalDate]
        .get mustBe date
    }
  }

  "NonStandardPeriodDate5Page" - {
    "must have the correct toString value" in {
      NonStandardPeriodDate5Page.toString mustBe "NonStandardPeriodDate5Page"
    }

    "must have the correct path" in {
      NonStandardPeriodDate5Page.path mustBe (JsPath \ "nstpEndDate5")
    }

    "must read and write LocalDate values correctly" in {
      val date = LocalDate.of(2026, 5, 1)
      val json = Json.obj("nstpEndDate5" -> Json.toJson(date))

      NonStandardPeriodDate5Page.path
        .asSingleJson(json)
        .validate[LocalDate]
        .get mustBe date
    }
  }

  "NonStandardPeriodDate6Page" - {
    "must have the correct toString value" in {
      NonStandardPeriodDate6Page.toString mustBe "NonStandardPeriodDate6Page"
    }

    "must have the correct path" in {
      NonStandardPeriodDate6Page.path mustBe (JsPath \ "nstpEndDate6")
    }

    "must read and write LocalDate values correctly" in {
      val date = LocalDate.of(2026, 6, 1)
      val json = Json.obj("nstpEndDate6" -> Json.toJson(date))

      NonStandardPeriodDate6Page.path
        .asSingleJson(json)
        .validate[LocalDate]
        .get mustBe date
    }
  }

  "NonStandardPeriodDate7Page" - {
    "must have the correct toString value" in {
      NonStandardPeriodDate7Page.toString mustBe "NonStandardPeriodDate7Page"
    }

    "must have the correct path" in {
      NonStandardPeriodDate7Page.path mustBe (JsPath \ "nstpEndDate7")
    }

    "must read and write LocalDate values correctly" in {
      val date = LocalDate.of(2026, 7, 1)
      val json = Json.obj("nstpEndDate7" -> Json.toJson(date))

      NonStandardPeriodDate7Page.path
        .asSingleJson(json)
        .validate[LocalDate]
        .get mustBe date
    }
  }

  "NonStandardPeriodDate8Page" - {
    "must have the correct toString value" in {
      NonStandardPeriodDate8Page.toString mustBe "NonStandardPeriodDate8Page"
    }

    "must have the correct path" in {
      NonStandardPeriodDate8Page.path mustBe (JsPath \ "nstpEndDate8")
    }

    "must read and write LocalDate values correctly" in {
      val date = LocalDate.of(2026, 8, 1)
      val json = Json.obj("nstpEndDate8" -> Json.toJson(date))

      NonStandardPeriodDate8Page.path
        .asSingleJson(json)
        .validate[LocalDate]
        .get mustBe date
    }
  }

  "NonStandardPeriodDatePages" - {

    "must return the correct page for each period number" in {
      NonStandardPeriodDatePages(1) mustBe NonStandardPeriodDate1Page
      NonStandardPeriodDatePages(2) mustBe NonStandardPeriodDate2Page
      NonStandardPeriodDatePages(3) mustBe NonStandardPeriodDate3Page
      NonStandardPeriodDatePages(4) mustBe NonStandardPeriodDate4Page
      NonStandardPeriodDatePages(5) mustBe NonStandardPeriodDate5Page
      NonStandardPeriodDatePages(6) mustBe NonStandardPeriodDate6Page
      NonStandardPeriodDatePages(7) mustBe NonStandardPeriodDate7Page
      NonStandardPeriodDatePages(8) mustBe NonStandardPeriodDate8Page
    }
  }
}
