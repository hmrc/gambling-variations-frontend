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

    "must assert the value at the path, in the shape of the answers" in {
      val modified = ModifiedAnswers().set(fieldPath, JsString("field")).success.value

      modified.data mustBe Json.obj("object" -> Json.obj("field" -> "field"))
    }

    "must replace what was asserted at or below the path" in {
      val modified = ModifiedAnswers()
        .set(fieldPath, JsString("field"))
        .flatMap(_.set(JsPath \ "other", JsString("other")))
        .flatMap(_.set(objectPath, Json.obj("a" -> 1)))
        .success
        .value

      modified.data mustBe Json.obj("object" -> Json.obj("a" -> 1), "other" -> "other")
    }

    "must keep what was asserted beside the path" in {
      val modified = ModifiedAnswers()
        .set(objectPath, Json.obj("a" -> 1))
        .flatMap(_.set(fieldPath, JsString("field")))
        .success
        .value

      modified.data mustBe Json.obj("object" -> Json.obj("a" -> 1, "field" -> "field"))
    }

    "must give way to an object when asserting below a tombstone" in {
      val modified = ModifiedAnswers()
        .remove(objectPath)
        .flatMap(_.set(fieldPath, JsString("field")))
        .success
        .value

      modified.data mustBe Json.obj("object" -> Json.obj("field" -> "field"))
    }

    "must be the same whatever order the assertions were made in" in {
      val first = ModifiedAnswers().set(JsPath \ "b", JsString("b")).flatMap(_.set(JsPath \ "a", JsString("a"))).success.value
      val second = ModifiedAnswers().set(JsPath \ "a", JsString("a")).flatMap(_.set(JsPath \ "b", JsString("b"))).success.value

      first mustBe second
    }

    "must refuse a path into a list" in {
      ModifiedAnswers().set(JsPath \ "list" \ 0, JsString("a")).isFailure mustBe true
    }

    "must refuse an empty path" in {
      ModifiedAnswers().set(JsPath, JsString("a")).isFailure mustBe true
    }
  }

  "get" - {

    "must return what is asserted at the path" in {
      val modified = ModifiedAnswers().set(fieldPath, JsString("field")).success.value

      modified.get[String](fieldPath).value mustBe "field"
      modified.get[JsObject](objectPath).value mustBe Json.obj("field" -> "field")
    }

    "must return nothing for a tombstone" in {
      ModifiedAnswers().remove(fieldPath).success.value.get[JsValue](fieldPath) mustBe None
    }
  }

  "retract" - {

    "must drop what is asserted at or below the path and keep the rest" in {
      val modified = ModifiedAnswers()
        .set(objectPath, Json.obj("field" -> "value"))
        .flatMap(_.set(fieldPath, JsString("field")))
        .flatMap(_.remove(JsPath \ "other"))
        .success
        .value

      modified.retract(objectPath).data mustBe Json.obj("other" -> JsNull)
    }

    "must leave the object above the path in place, even when it is empty" in {
      val modified = ModifiedAnswers().set(fieldPath, JsString("field")).success.value

      modified.retract(fieldPath).data mustBe Json.obj("object" -> Json.obj())
    }

    "must change nothing below a tombstone" in {
      val modified = ModifiedAnswers().remove(objectPath).success.value

      modified.retract(fieldPath) mustBe modified
    }
  }

  "applyTo" - {

    "must merge over the original answers, where a null removes, an object merges and anything else replaces" in {
      val modified = ModifiedAnswers()
        .remove(JsPath \ "removed")
        .flatMap(_.set(fieldPath, JsString("new")))
        .flatMap(_.set(JsPath \ "list", Json.arr(1)))
        .success
        .value

      val original = Json.obj(
        "removed" -> "value",
        "object"  -> Json.obj("field" -> "old", "kept" -> true),
        "list"    -> Json.arr(1, 2, 3)
      )

      modified.applyTo(original) mustBe Json.obj("object" -> Json.obj("field" -> "new", "kept" -> true), "list" -> Json.arr(1))
    }

    "must put an object where the original has none, or has something else" in {
      val modified = ModifiedAnswers().set(fieldPath, JsString("new")).success.value

      modified.applyTo(Json.obj()) mustBe Json.obj("object" -> Json.obj("field" -> "new"))
      modified.applyTo(Json.obj("object" -> "text")) mustBe Json.obj("object" -> Json.obj("field" -> "new"))
    }
  }

  "format" - {

    "must write and read the assertions as they are" in {
      val modified = ModifiedAnswers().set(fieldPath, JsString("field")).flatMap(_.remove(JsPath \ "other")).success.value

      Json.toJson(modified) mustBe Json.obj("object" -> Json.obj("field" -> "field"), "other" -> JsNull)
      Json.toJson(modified).as[ModifiedAnswers] mustBe modified
    }
  }
}
