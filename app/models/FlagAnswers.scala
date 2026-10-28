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

import scala.util.{Success, Try}

// Journey state that is never sent to the backend, such as screener answers and the submitted flags
final case class FlagAnswers(data: JsObject = Json.obj()) extends AnswerStore[FlagAnswers] {

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    AnswerStore.read(data, path)

  def set(path: JsPath, value: JsValue): Try[FlagAnswers] =
    AnswerStore.write(data, path, value).map(FlagAnswers(_))

  def remove(path: JsPath): Try[FlagAnswers] =
    Success(FlagAnswers(AnswerStore.erase(data, path)))
}

object FlagAnswers {

  given format: Format[FlagAnswers] =
    Format.of[JsObject].bimap(FlagAnswers(_), _.data)
}
