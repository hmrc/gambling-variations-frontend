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

import play.api.libs.json.*

import scala.util.{Failure, Success, Try}

// Everything the user asserted in this session, in the same shape as the answers, read as a JSON Merge Patch (RFC 7396)
// over the original answers: a value is asserted, whether or not it equals the original, a null is a tombstone that
// asserts the answer is absent, and a key that is not there is not asserted. A list is asserted whole, since removing
// an element shifts the position of the ones after it.
final case class ModifiedAnswers(data: JsObject = Json.obj()) extends AnswerStore[ModifiedAnswers] {

  // The value asserted at the path, if the answer was not asserted to be absent
  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    path.asSingleJson(data).toOption.filterNot(_ == JsNull).flatMap(_.asOpt[A])

  // Asserts the value at the path, in place of whatever was asserted at or below it. A tombstone or a plain value above
  // the path gives way to an object, so the caller asserts at that ancestor instead when what it hid must stay hidden.
  def set(path: JsPath, value: JsValue): Try[ModifiedAnswers] =
    ModifiedAnswers.keysOf(path) match {
      case Some(keys) if keys.nonEmpty => Success(ModifiedAnswers(ModifiedAnswers.put(data, keys, value)))
      case _                           => Failure(new IllegalArgumentException(s"Answers are asserted at keys and as whole lists only, not at $path"))
    }

  // A tombstone at the path
  def remove(path: JsPath): Try[ModifiedAnswers] =
    set(path, JsNull)

  // Drops what is asserted at or below the path, so that nothing is asserted there any more
  def retract(path: JsPath): ModifiedAnswers =
    ModifiedAnswers.keysOf(path).filter(_.nonEmpty).fold(this)(keys => ModifiedAnswers(ModifiedAnswers.drop(data, keys)))

  // The answers as the journey sees them, which is the assertions merged over the original answers
  def applyTo(original: JsObject): JsObject =
    ModifiedAnswers.merge(original, data)
}

object ModifiedAnswers {

  given format: Format[ModifiedAnswers] =
    Format.of[JsObject].bimap(ModifiedAnswers(_), _.data)

  // JSON Merge Patch: a null removes the key, an object is merged into the object that is there, and any other value
  // replaces what is there
  def merge(target: JsObject, patch: JsObject): JsObject =
    patch.fields.foldLeft(target) {
      case (merged, (key, JsNull))           => merged - key
      case (merged, (key, nested: JsObject)) => merged + (key -> merge((merged \ key).asOpt[JsObject].getOrElse(Json.obj()), nested))
      case (merged, (key, value))            => merged + (key -> value)
    }

  private def keysOf(path: JsPath): Option[List[String]] =
    Option.when(path.path.forall(_.isInstanceOf[KeyPathNode]))(path.path.collect { case KeyPathNode(key) => key })

  private def put(node: JsObject, keys: List[String], value: JsValue): JsObject =
    keys match {
      case key :: Nil  => node + (key -> value)
      case key :: rest => node + (key -> put((node \ key).asOpt[JsObject].getOrElse(Json.obj()), rest, value))
      case Nil         => node
    }

  private def drop(node: JsObject, keys: List[String]): JsObject =
    keys match {
      case key :: Nil  => node - key
      case key :: rest => (node \ key).asOpt[JsObject].fold(node)(child => node + (key -> drop(child, rest)))
      case Nil         => node
    }
}
