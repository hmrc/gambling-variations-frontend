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

import models.controllingbody.ControllingBodyChangeOption.*
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsString, Json}

class ControllingBodyChangeOptionSpec extends AnyFreeSpec with Matchers with OptionValues {

  "ControllingBodyChangeOption" - {

    "must list the options in the order they are shown on the page" in {
      ControllingBodyChangeOption.values mustBe Seq(EditDetails, ProvideNew, KeepSame)
    }

    "must find every option by its name" in {
      ControllingBodyChangeOption.enumerable.withName("editDetails").value mustBe EditDetails
      ControllingBodyChangeOption.enumerable.withName("provideNew").value mustBe ProvideNew
      ControllingBodyChangeOption.enumerable.withName("keepSame").value mustBe KeepSame
    }

    "must not find an unknown name" in {
      ControllingBodyChangeOption.enumerable.withName("unknown") mustBe None
    }

    "must write to JSON as its name" in {
      Json.toJson[ControllingBodyChangeOption](ProvideNew) mustBe JsString("provideNew")
    }

    "must read from JSON by its name" in {
      JsString("keepSame").as[ControllingBodyChangeOption] mustBe KeepSame
    }

    "must fail to read an unknown JSON value" in {
      JsString("unknown").validate[ControllingBodyChangeOption] mustBe JsError("error.invalid")
    }
  }
}
