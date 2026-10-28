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

import java.time.Instant
import scala.util.{Success, Try}

// The answers as loaded from the backend, in the same shape as the answers the journey sees, and when each section was
// loaded (by section key). Only the section loaders write here. The two are kept as separate fields of the session
// document, so that the original answers keep their shape there as well.
final case class OriginalAnswers(
  data: JsObject = Json.obj(),
  loaded: Map[String, Instant] = Map.empty
) extends AnswerStore[OriginalAnswers] {

  def get[A](path: JsPath)(implicit rds: Reads[A]): Option[A] =
    AnswerStore.read(data, path)

  def set(path: JsPath, value: JsValue): Try[OriginalAnswers] =
    AnswerStore.write(data, path, value).map(updated => copy(data = updated))

  def remove(path: JsPath): Try[OriginalAnswers] =
    Success(copy(data = AnswerStore.erase(data, path)))

  def isLoaded(section: Section): Boolean =
    loaded.contains(section.key)

  // Whatever the section held before is dropped, so that a section can also be loaded again
  def withSectionLoaded(section: Section, loadedAt: Instant): OriginalAnswers =
    copy(data = AnswerStore.erase(data, section.path), loaded = loaded + (section.key -> loadedAt))
}
