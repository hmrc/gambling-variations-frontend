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

package pages
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.JsPath

class BusinessTypePageSpec extends AnyFreeSpec with Matchers {

  ".BusinessTypePage" - {

    "must have the correct toString" in {
      BusinessTypePage.toString mustBe "businessType"
    }

    "must have a path corresponding to its name" in {
      val expectedPath: JsPath = JsPath \ "businessType"
      BusinessTypePage.path mustBe expectedPath
    }
  }
}
