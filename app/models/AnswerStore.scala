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

// The contract shared by the stores that make up UserAnswers. Each store encapsulates its own JSON.
trait AnswerStore[S <: AnswerStore[S]] {

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A]

  def set(path: JsPath, value: JsValue): Try[S]

  def remove(path: JsPath): Try[S]
}

object AnswerStore {

  def read[A](data: JsObject, path: JsPath)(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(path)).reads(data).getOrElse(None)

  def write(data: JsObject, path: JsPath, value: JsValue): Try[JsObject] =
    data.setObject(path, value) match {
      case JsSuccess(updated, _) => Success(updated)
      case JsError(errors)       => Failure(JsResultException(errors))
    }

  // Removing a value that is not there leaves the data as it was
  def erase(data: JsObject, path: JsPath): JsObject =
    data.removeObject(path).getOrElse(data)
}
