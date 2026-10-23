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

import models.controllingbody.ControllingBodyCacheMigration
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import queries.{Flag, Gettable, Settable}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.Try

// The session state of one registration: the answers loaded from the backend, the answers asserted by the user in this
// session, and the journey flags. Pages read and write it through get, set and remove, whichever store they belong to.
final case class UserAnswers(
  id: String,
  original: OriginalAnswers = OriginalAnswers(),
  modified: ModifiedAnswers = ModifiedAnswers(),
  flags: FlagAnswers = FlagAnswers(),
  lastUpdated: Instant = Instant.now
) {

  // The answers as the journey sees them, which is the assertions replayed over the original answers
  lazy val data: JsObject = modified.applyTo(original.data)

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    page match {
      case _: Flag => flags.get(page.path)
      case _       => AnswerStore.read(data, page.path)
    }

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val json = Json.toJson(value)

    val updatedAnswers = page match {
      case _: Flag => flags.set(page.path, json).map(updatedFlags => copy(flags = updatedFlags))
      case _       => AnswerStore.write(data, page.path, json).flatMap(withAssertion(page.path, _))
    }

    updatedAnswers.flatMap(page.cleanup(Some(value), _))
  }

  def setIfDefined[A](page: Settable[A], optional: Option[A])(implicit wrt: Writes[A]): Try[UserAnswers] =
    optional.fold(Try(this)) { value =>
      set(page, value)
    }

  // Removing an answer drops what the session asserted at or below its path. A tombstone is written only where a
  // value would still show, which is where the original answers or an assertion further up hold one.
  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedAnswers = page match {
      case _: Flag                    => flags.remove(page.path).map(updatedFlags => copy(flags = updatedFlags))
      case _ if isKeysOnly(page.path) => withAbsence(page.path)
      case _ =>
        val updatedData = AnswerStore.erase(data, page.path)
        if (updatedData == data) Try(this) else withAssertion(page.path, updatedData)
    }

    updatedAnswers.flatMap(page.cleanup(None, _))
  }

  // Only the section loaders write the original answers, and this does not trigger any cleanup
  def setOriginal[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] =
    original.set(page.path, Json.toJson(value)).map(updatedOriginal => copy(original = updatedOriginal))

  def setOriginalIfDefined[A](page: Settable[A], optional: Option[A])(implicit writes: Writes[A]): Try[UserAnswers] =
    optional.fold(Try(this)) { value =>
      setOriginal(page, value)
    }

  def isLoaded(section: Section): Boolean =
    original.isLoaded(section)

  def withSectionLoaded(section: Section, loadedAt: Instant): UserAnswers =
    copy(original = original.withSectionLoaded(section, loadedAt))

  // The changes to submit for a section: what the journey shows at each asserted path, where that differs from the
  // original answers. An assertion below another one is covered by that one, so it is left out.
  def changes(section: Section): Seq[Assertion] =
    modified.assertions
      .filter(assertion => assertion.isAtOrBelow(section.path) && !isBelowAnother(assertion))
      .map(assertion => Assertion(assertion.path, valueAt(data, assertion.path)))
      .filterNot(change => change.value == valueAt(original.data, change.path))

  def hasChanges(section: Section): Boolean =
    changes(section).nonEmpty

  def partnersCount: Int =
    (data \ "partners").asOpt[JsObject].map(_.value.size).getOrElse(0)

  def newPartnersCount: Int =
    (data \ "newPartners").asOpt[JsArray].map(_.value.size).getOrElse(0)

  private def isKeysOnly(path: JsPath): Boolean =
    path.path.forall(_.isInstanceOf[KeyPathNode])

  private def withAbsence(path: JsPath): Try[UserAnswers] = {

    val retracted = modified.retract(path)

    if (valueAt(retracted.applyTo(original.data), path) == JsNull) {
      Try(copy(modified = retracted))
    } else {
      retracted.remove(path).map(updatedModified => copy(modified = updatedModified))
    }
  }

  private def isBelowAnother(assertion: Assertion): Boolean =
    modified.assertions.exists(other => other.path != assertion.path && assertion.isAtOrBelow(other.path))

  // The value at a path, with a null for no value, so that a tombstone and an absent value compare equal
  private def valueAt(json: JsObject, path: JsPath): JsValue =
    path.asSingleJson(json).toOption.getOrElse(JsNull)

  // A change inside a list is asserted as the whole list, as it is after the change
  private def withAssertion(path: JsPath, updatedData: JsObject): Try[UserAnswers] = {

    val assertedPath = JsPath(path.path.takeWhile(_.isInstanceOf[KeyPathNode]))
    val assertedValue = assertedPath.asSingleJson(updatedData).toOption.getOrElse(JsNull)

    modified.set(assertedPath, assertedValue).map(updatedModified => copy(modified = updatedModified))
  }
}

object UserAnswers {

  // Test fixtures provide plain JSON, which is taken as the answers loaded from the backend
  def apply(id: String, data: JsObject): UserAnswers =
    UserAnswers(id, OriginalAnswers(data))

  def apply(id: String, data: JsObject, lastUpdated: Instant): UserAnswers =
    UserAnswers(id, OriginalAnswers(data), lastUpdated = lastUpdated)

  val reads: Reads[UserAnswers] =
    (
      (__ \ "_id").read[String] and
        (__ \ "original").read[OriginalAnswers].map(original => original.copy(data = ControllingBodyCacheMigration(original.data))) and
        (__ \ "modified").read[ModifiedAnswers] and
        (__ \ "flags").read[FlagAnswers] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(new UserAnswers(_, _, _, _, _))

  val writes: OWrites[UserAnswers] =
    (
      (__ \ "_id").write[String] and
        (__ \ "original").write[OriginalAnswers] and
        (__ \ "modified").write[ModifiedAnswers] and
        (__ \ "flags").write[FlagAnswers] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(answers => (answers.id, answers.original, answers.modified, answers.flags, answers.lastUpdated))

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}
