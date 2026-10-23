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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import scala.util.{Failure, Success, Try}

// An answer asserted by the user in this session. A null value asserts that the answer is absent, which makes it a tombstone.
final case class Assertion(path: JsPath, value: JsValue) {

  def isRemoval: Boolean = value == JsNull

  def isAtOrBelow(ancestor: JsPath): Boolean =
    path.path.startsWith(ancestor.path)

  def keys: List[String] =
    path.path.collect { case KeyPathNode(key) => key }
}

object Assertion {

  // The path is held as its list of keys, since paths are awkward as Mongo field names
  private val pathFormat: Format[JsPath] = Format(
    Reads.of[List[String]].map(keys => JsPath(keys.map(KeyPathNode(_)))),
    Writes.of[List[String]].contramap(_.path.collect { case KeyPathNode(key) => key })
  )

  given format: OFormat[Assertion] = (
    (__ \ "path").format(pathFormat) and
      (__ \ "value").format[JsValue]
  )(Assertion.apply, assertion => (assertion.path, assertion.value))
}

// Everything the user asserted in this session, including values equal to the original. An assertion replaces those
// at or below its path, so every remaining assertion was made after any assertion above it. They are kept in path
// order, where a path comes before the paths below it, so replaying them in order over the original answers gives
// the answers as the journey sees them, and answers that hold the same assertions are equal.
final case class ModifiedAnswers(assertions: Seq[Assertion] = Nil) extends AnswerStore[ModifiedAnswers] {

  // The value asserted at exactly this path, if it was not asserted to be absent
  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    assertions.find(_.path == path).filterNot(_.isRemoval).flatMap(_.value.asOpt[A])

  // A list is asserted as a whole, since removing an element shifts the position of the ones after it
  def set(path: JsPath, value: JsValue): Try[ModifiedAnswers] =
    if (path.path.nonEmpty && path.path.forall(_.isInstanceOf[KeyPathNode])) {
      val updated = assertions.filterNot(_.isAtOrBelow(path)) :+ Assertion(path, value)
      Success(copy(assertions = updated.sortBy(_.keys)(using Ordering.Implicits.seqOrdering)))
    } else {
      Failure(new IllegalArgumentException(s"Assertions are made on keys and whole lists only, not on $path"))
    }

  def remove(path: JsPath): Try[ModifiedAnswers] =
    set(path, JsNull)

  // Drops the assertions at or below the path, so that nothing is asserted there any more
  def retract(path: JsPath): ModifiedAnswers =
    copy(assertions = assertions.filterNot(_.isAtOrBelow(path)))

  def applyTo(original: JsObject): JsObject =
    assertions.foldLeft(original) { (data, assertion) =>
      if (assertion.isRemoval) {
        AnswerStore.erase(data, assertion.path)
      } else {
        AnswerStore.write(data, assertion.path, assertion.value).getOrElse(data)
      }
    }
}

object ModifiedAnswers {

  given format: Format[ModifiedAnswers] =
    Format.of[Seq[Assertion]].bimap(ModifiedAnswers(_), _.assertions)
}
