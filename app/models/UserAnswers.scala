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

  // The changes to submit for a section, as a JSON Merge Patch over the original answers: what the user asserted in the
  // section where it differs from the original, with a null where an original value was removed. An object with no
  // change inside is left out, and so is the removal of a value that was never there.
  def changes(section: Section): JsObject =
    section.path
      .asSingleJson(modified.data)
      .toOption
      .flatMap(asserted => difference(valueAt(original.data, section.path), asserted))
      .flatMap(change => AnswerStore.write(Json.obj(), section.path, change).toOption)
      .getOrElse(Json.obj())

  def hasChanges(section: Section): Boolean =
    changes(section).fields.nonEmpty

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

  // The part of an asserted value that differs from the original, or nothing when it is all the same. A tombstone and
  // an absent value compare equal, and an object with no change inside counts as no change.
  private def difference(original: JsValue, asserted: JsValue): Option[JsValue] =
    (original, asserted) match {
      case (JsNull, JsNull)                                     => None
      case (_, JsNull)                                          => Some(JsNull)
      case (originalObject: JsObject, assertedObject: JsObject) => differences(originalObject, assertedObject)
      case (_, assertedObject: JsObject)                        => differences(Json.obj(), assertedObject)
      case (_, value)                                           => Option.when(value != original)(value)
    }

  private def differences(originalObject: JsObject, assertedObject: JsObject): Option[JsObject] = {

    val changed = assertedObject.fields.flatMap { case (key, value) =>
      difference(valueAt(originalObject, JsPath \ key), value).map(key -> _)
    }

    Option.when(changed.nonEmpty)(JsObject(changed))
  }

  // The value at a path, with a null for no value, so that a tombstone and an absent value compare equal
  private def valueAt(json: JsObject, path: JsPath): JsValue =
    path.asSingleJson(json).toOption.getOrElse(JsNull)

  // A change inside a list is asserted as the whole list, as it is after the change. The asserted value carries a
  // tombstone for every key the original holds under the path and the new value does not, so that it replaces the
  // original value whole. Below a tombstone, or below a value asserted as something other than an object, the
  // assertion is made at that ancestor instead, so that what the ancestor hid stays hidden.
  private def withAssertion(path: JsPath, updatedData: JsObject): Try[UserAnswers] = {

    val keyPath = JsPath(path.path.takeWhile(_.isInstanceOf[KeyPathNode]))
    val assertedPath = ancestorsOf(keyPath).find(hidesBelow).getOrElse(keyPath)
    val assertedValue = withTombstones(valueAt(original.data, assertedPath), valueAt(updatedData, assertedPath))

    modified.set(assertedPath, assertedValue).map(updatedModified => copy(modified = updatedModified))
  }

  // The proper ancestors of a path, from the top down
  private def ancestorsOf(path: JsPath): Seq[JsPath] =
    (1 until path.path.size).map(length => JsPath(path.path.take(length)))

  // Whether what is asserted at the path hides everything below it: a tombstone, or a value that is not an object
  private def hidesBelow(path: JsPath): Boolean =
    path.asSingleJson(modified.data).toOption.exists(!_.isInstanceOf[JsObject])

  private def withTombstones(original: JsValue, asserted: JsValue): JsValue =
    (original, asserted) match {
      case (originalObject: JsObject, assertedObject: JsObject) =>
        val kept = assertedObject.fields.map { case (key, value) => key -> withTombstones(valueAt(originalObject, JsPath \ key), value) }
        val removed = (originalObject.keys -- assertedObject.keys).toSeq.map(_ -> JsNull)
        JsObject(kept ++ removed)
      case (_, value) => value
    }
}

object UserAnswers {

  private given instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  // Test fixtures provide plain JSON, which is taken as the answers loaded from the backend
  def apply(id: String, data: JsObject): UserAnswers =
    UserAnswers(id, OriginalAnswers(data))

  def apply(id: String, data: JsObject, lastUpdated: Instant): UserAnswers =
    UserAnswers(id, OriginalAnswers(data), lastUpdated = lastUpdated)

  // The original answers and the time each section was loaded are separate fields of the document, so that the
  // original and the modified answers both keep the shape of the answers
  val reads: Reads[UserAnswers] =
    (
      (__ \ "_id").read[String] and
        (__ \ "original").read[JsObject].map(ControllingBodyCacheMigration(_)) and
        (__ \ "loaded").read[Map[String, Instant]] and
        (__ \ "modified").read[ModifiedAnswers] and
        (__ \ "flags").read[FlagAnswers] and
        (__ \ "lastUpdated").read[Instant]
    )((id, original, loaded, modified, flags, lastUpdated) => new UserAnswers(id, OriginalAnswers(original, loaded), modified, flags, lastUpdated))

  val writes: OWrites[UserAnswers] =
    (
      (__ \ "_id").write[String] and
        (__ \ "original").write[JsObject] and
        (__ \ "loaded").write[Map[String, Instant]] and
        (__ \ "modified").write[ModifiedAnswers] and
        (__ \ "flags").write[FlagAnswers] and
        (__ \ "lastUpdated").write[Instant]
    )(answers => (answers.id, answers.original.data, answers.original.loaded, answers.modified, answers.flags, answers.lastUpdated))

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}
