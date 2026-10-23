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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import play.api.libs.json.*

class ModifiedAnswersSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  private val objectPath = JsPath \ "object"
  private val fieldPath = JsPath \ "object" \ "field"

  "set" - {

    "must replace the assertions at or below the path" in {
      val modified = ModifiedAnswers()
        .set(fieldPath, JsString("field"))
        .flatMap(_.set(JsPath \ "other", JsString("other")))
        .flatMap(_.set(objectPath, Json.obj("a" -> 1)))
        .success
        .value

      modified.assertions mustBe Seq(
        Assertion(objectPath, Json.obj("a" -> 1)),
        Assertion(JsPath \ "other", JsString("other"))
      )
    }

    "must keep the assertions in path order, whatever order they were made in" in {
      val first = ModifiedAnswers().set(JsPath \ "b", JsString("b")).flatMap(_.set(JsPath \ "a", JsString("a"))).success.value
      val second = ModifiedAnswers().set(JsPath \ "a", JsString("a")).flatMap(_.set(JsPath \ "b", JsString("b"))).success.value

      first mustBe second
    }

    "must keep an assertion below an earlier assertion after it" in {
      val modified = ModifiedAnswers()
        .set(objectPath, Json.obj("a" -> 1))
        .flatMap(_.set(fieldPath, JsString("field")))
        .success
        .value

      modified.applyTo(Json.obj()) mustBe Json.obj("object" -> Json.obj("a" -> 1, "field" -> "field"))
    }

    "must refuse a path into a list" in {
      ModifiedAnswers().set(JsPath \ "list" \ 0, JsString("a")).isFailure mustBe true
    }

    "must refuse an empty path" in {
      ModifiedAnswers().set(JsPath, JsString("a")).isFailure mustBe true
    }
  }

  "get" - {

    "must return the value asserted at exactly the path" in {
      val modified = ModifiedAnswers().set(fieldPath, JsString("field")).success.value

      modified.get[String](fieldPath).value mustBe "field"
      modified.get[JsObject](objectPath) mustBe None
    }

    "must return nothing for a tombstone" in {
      ModifiedAnswers().remove(fieldPath).success.value.get[JsValue](fieldPath) mustBe None
    }
  }

  "retract" - {

    "must drop the assertions at or below the path and keep the others" in {
      val modified = ModifiedAnswers()
        .set(objectPath, Json.obj("field" -> "value"))
        .flatMap(_.set(fieldPath, JsString("field")))
        .flatMap(_.remove(JsPath \ "other"))
        .success
        .value

      modified.retract(objectPath).assertions mustBe Seq(Assertion(JsPath \ "other", JsNull))
    }
  }

  "applyTo" - {

    "must replay the assertions over the original answers" in {
      val modified = ModifiedAnswers()
        .remove(JsPath \ "removed")
        .flatMap(_.set(fieldPath, JsString("new")))
        .success
        .value

      val original = Json.obj("removed" -> "value", "object" -> Json.obj("field" -> "old", "kept" -> true))

      modified.applyTo(original) mustBe Json.obj("object" -> Json.obj("field" -> "new", "kept" -> true))
    }
  }
}
