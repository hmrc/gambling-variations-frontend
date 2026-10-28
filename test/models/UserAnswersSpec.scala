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
import pages.QuestionPage
import pages.contactdetails.*
import play.api.libs.json.*
import queries.{Gettable, Settable}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.Try

class UserAnswersSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  import UserAnswersSpec.*

  private val section = BusinessContactDetailsSection
  private val loadedAt = Instant.parse("2026-10-20T09:00:00.123Z")

  private val contactNumber = ContactNumber(Some("01632 960001"), Some("07700 900001"))

  private val loadedAnswers: UserAnswers =
    UserAnswers("id")
      .withSectionLoaded(section, loadedAt)
      .setOriginal(BusinessContactNumberPage, contactNumber)
      .flatMap(_.setOriginal(BusinessEmailAddressPage, "info@example.com"))
      .flatMap(_.setOriginal(BusinessFaxNumberPage, "01632 960002"))
      .success
      .value

  // Answers in the Business Contact Details section, in the shape the stores hold them
  private def contactDetails(fields: (String, Json.JsValueWrapper)*): JsObject =
    Json.obj("businessContactDetailsSection" -> Json.obj(fields*))

  "setOriginal" - {

    "must write the original answers without asserting anything" in {
      loadedAnswers.original.get[String](BusinessEmailAddressPage.path).value mustBe "info@example.com"
      loadedAnswers.get(BusinessEmailAddressPage).value mustBe "info@example.com"
      loadedAnswers.modified.data mustBe Json.obj()
    }

    "must not run the cleanup of the page" in {
      val answers = UserAnswers("id").setOriginal(PageWithCleanup, "value").success.value

      answers.get(CleanedPage) mustBe None
      answers.modified.data mustBe Json.obj()
    }
  }

  "withSectionLoaded" - {

    "must mark the section as loaded and drop what the section held before" in {
      val reloaded = loadedAnswers.withSectionLoaded(section, loadedAt.plusSeconds(60))

      reloaded.isLoaded(section) mustBe true
      reloaded.original.loaded mustBe Map(section.key -> loadedAt.plusSeconds(60))
      reloaded.get(BusinessEmailAddressPage) mustBe None
    }

    "must leave other sections as they were" in {
      val answers = UserAnswers("id", Json.obj("otherSection" -> Json.obj("value" -> 1)))

      answers.withSectionLoaded(section, loadedAt).data mustBe Json.obj("otherSection" -> Json.obj("value" -> 1))
    }
  }

  "set" - {

    "must assert the value, leaving the original answers as they were" in {
      val answers = loadedAnswers.set(BusinessEmailAddressPage, "contact@example.com").success.value

      answers.get(BusinessEmailAddressPage).value mustBe "contact@example.com"
      answers.original mustBe loadedAnswers.original
      answers.modified.data mustBe contactDetails("businessEmailAddress" -> "contact@example.com")
    }

    "must replace the value as a whole, so that a missing optional field is cleared with a tombstone" in {
      val answers = loadedAnswers.set(BusinessContactNumberPage, ContactNumber(Some("01632 960009"), None)).success.value

      answers.get(BusinessContactNumberPage).value mustBe ContactNumber(Some("01632 960009"), None)
      answers.modified.data mustBe contactDetails(
        "businessContactNumber" -> Json.obj("phoneNumber" -> "01632 960009", "mobilePhoneNumber" -> JsNull)
      )
    }

    "must record a value equal to the original as asserted" in {
      val answers = loadedAnswers.set(BusinessEmailAddressPage, "info@example.com").success.value

      answers.modified.get[String](BusinessEmailAddressPage.path).value mustBe "info@example.com"
    }

    "must assert a change inside a list as the whole list" in {
      val answers = UserAnswers("id", Json.obj("list" -> Json.arr("a", "b")))
        .set(ListElement(1), JsString("c"))
        .success
        .value

      answers.data mustBe Json.obj("list" -> Json.arr("a", "c"))
      answers.modified.data mustBe Json.obj("list" -> Json.arr("a", "c"))
    }

    "must fail when the value cannot be written at the path" in {
      UserAnswers("id", Json.obj("list" -> Json.arr("a"))).set(ListElement(3), JsString("c")).isFailure mustBe true
    }

    "must run the cleanup of the page" in {
      val answers = UserAnswers("id", Json.obj("cleaned" -> "original"))
        .set(PageWithCleanup, "value")
        .success
        .value

      answers.get(CleanedPage) mustBe None
      answers.modified.data mustBe Json.obj("withCleanup" -> "value", "cleaned" -> JsNull)
    }

    "must keep flags in the flags store" in {
      val answers = loadedAnswers.set(BusinessContactDetailsSubmittedPage, true).success.value

      answers.get(BusinessContactDetailsSubmittedPage).value mustBe true
      answers.flags.data mustBe Json.obj("businessContactDetailsSection" -> Json.obj("submitted" -> true))
      answers.data mustBe loadedAnswers.data
      answers.modified.data mustBe Json.obj()
    }
  }

  "remove" - {

    "must assert that an original value is absent, so that it does not show again" in {
      val answers = loadedAnswers.remove(BusinessFaxNumberPage).success.value

      answers.get(BusinessFaxNumberPage) mustBe None
      answers.original mustBe loadedAnswers.original
      answers.modified.data mustBe contactDetails("businessFaxNumber" -> JsNull)
    }

    "must change nothing when there is no value to remove" in {
      val answers = UserAnswers("id")

      answers.remove(BusinessFaxNumberPage).success.value mustBe answers
    }

    "must drop a value asserted earlier in the session, with no tombstone when the original has nothing" in {
      val answers = UserAnswers("id")
        .set(BusinessFaxNumberPage, "01632 960002")
        .flatMap(_.remove(BusinessFaxNumberPage))
        .success
        .value

      answers.get(BusinessFaxNumberPage) mustBe None
      answers.modified.data mustBe contactDetails()
    }

    "must assert absence of a value that replaced an original value earlier in the session" in {
      val answers = loadedAnswers
        .set(BusinessFaxNumberPage, "01632 960002")
        .flatMap(_.remove(BusinessFaxNumberPage))
        .success
        .value

      answers.get(BusinessFaxNumberPage) mustBe None
      answers.modified.data mustBe contactDetails("businessFaxNumber" -> JsNull)
    }

    "must take a value out of an object asserted further up" in {
      val answers = UserAnswers("id")
        .set(ObjectQuery, Json.obj("a" -> 1, "b" -> 2))
        .flatMap(_.remove(ObjectField("a")))
        .success
        .value

      answers.data mustBe Json.obj("object" -> Json.obj("b" -> 2))
      answers.modified.data mustBe Json.obj("object" -> Json.obj("b" -> 2))
    }

    "must not write a tombstone below one that already hides the value" in {
      val removed = UserAnswers("id", Json.obj("object" -> Json.obj("a" -> 1))).remove(ObjectQuery).success.value

      removed.remove(ObjectField("a")).success.value mustBe removed
    }

    "must remove an element of a list by asserting the whole list" in {
      val answers = UserAnswers("id", Json.obj("list" -> Json.arr("a", "b", "c")))
        .remove(ListElement(0))
        .flatMap(_.remove(ListElement(0)))
        .success
        .value

      answers.data mustBe Json.obj("list" -> Json.arr("c"))
      answers.modified.data mustBe Json.obj("list" -> Json.arr("c"))
    }

    "must remove a flag from the flags store only" in {
      val answers = loadedAnswers
        .set(RemoveFaxNumberPage, true)
        .flatMap(_.remove(RemoveFaxNumberPage))
        .success
        .value

      answers.get(RemoveFaxNumberPage) mustBe None
      answers.modified.data mustBe Json.obj()
    }

    "must assert absence of a whole object, and a later value inside it must not bring back the rest" in {
      val answers = UserAnswers("id", Json.obj("object" -> Json.obj("a" -> 1, "b" -> 2)))
        .remove(ObjectQuery)
        .flatMap(_.set(ObjectField("a"), 3))
        .success
        .value

      answers.data mustBe Json.obj("object" -> Json.obj("a" -> 3))
      answers.modified.data mustBe Json.obj("object" -> Json.obj("a" -> 3, "b" -> JsNull))
    }
  }

  "changes" - {

    "must give what was asserted in the section and differs from the original answers, as a merge patch" in {
      val answers = loadedAnswers
        .set(BusinessEmailAddressPage, "contact@example.com")
        .flatMap(_.remove(BusinessFaxNumberPage))
        .flatMap(_.set(BusinessContactNumberPage, contactNumber))
        .success
        .value

      answers.changes(section) mustBe contactDetails("businessEmailAddress" -> "contact@example.com", "businessFaxNumber" -> JsNull)
      answers.hasChanges(section) mustBe true
    }

    "must not contain the absence of a value that was never there" in {
      val answers = UserAnswers("id")
        .withSectionLoaded(section, loadedAt)
        .set(BusinessFaxNumberPage, "01632 960002")
        .flatMap(_.remove(BusinessFaxNumberPage))
        .success
        .value

      answers.changes(section) mustBe Json.obj()
      answers.hasChanges(section) mustBe false
    }

    "must not contain assertions in other sections" in {
      val answers = loadedAnswers.set(ObjectField("a"), 1).success.value

      answers.changes(section) mustBe Json.obj()
    }

    "must give an object as the journey sees it, after a removal inside it" in {
      val answers = UserAnswers("id")
        .withSectionLoaded(section, loadedAt)
        .set(BusinessContactNumberPage, contactNumber)
        .flatMap(_.remove(ContactNumberField("mobilePhoneNumber")))
        .success
        .value

      answers.changes(section) mustBe contactDetails("businessContactNumber" -> Json.obj("phoneNumber" -> "01632 960001"))
    }

    "must give a changed field and a removed field inside an object" in {
      val answers = loadedAnswers
        .set(BusinessContactNumberPage, ContactNumber(Some("01632 960009"), Some("07700 900009")))
        .flatMap(_.remove(ContactNumberField("mobilePhoneNumber")))
        .success
        .value

      answers.changes(section) mustBe contactDetails(
        "businessContactNumber" -> Json.obj("phoneNumber" -> "01632 960009", "mobilePhoneNumber" -> JsNull)
      )
    }
  }

  "format" - {

    "must write and read all of the stores" in {
      val answers = loadedAnswers
        .set(BusinessEmailAddressPage, "contact@example.com")
        .flatMap(_.remove(BusinessFaxNumberPage))
        .flatMap(_.set(BusinessContactDetailsSubmittedPage, true))
        .success
        .value
        .copy(lastUpdated = loadedAt)

      Json.toJson(answers).as[UserAnswers] mustBe answers
    }

    "must keep the original and the modified answers in the shape of the answers, with the load marks beside them" in {
      val answers = loadedAnswers.remove(BusinessFaxNumberPage).success.value
      val json = Json.toJson(answers).as[JsObject]

      json.keys mustBe Set("_id", "original", "loaded", "modified", "flags", "lastUpdated")
      (json \ "original").as[JsObject] mustBe loadedAnswers.original.data
      (json \ "loaded" \ section.key).as[Instant](MongoJavatimeFormats.instantFormat) mustBe loadedAt
      (json \ "modified").as[JsObject] mustBe contactDetails("businessFaxNumber" -> JsNull)
    }
  }
}

object UserAnswersSpec {

  case class ListElement(index: Int) extends Gettable[JsString] with Settable[JsString] {
    override def path: JsPath = JsPath \ "list" \ index
  }

  case object ObjectQuery extends Gettable[JsObject] with Settable[JsObject] {
    override def path: JsPath = JsPath \ "object"
  }

  case class ObjectField(key: String) extends Gettable[Int] with Settable[Int] {
    override def path: JsPath = JsPath \ "object" \ key
  }

  // A field inside the contact number, as a composite page would have it
  case class ContactNumberField(key: String) extends Gettable[String] with Settable[String] {
    override def path: JsPath = BusinessContactNumberPage.path \ key
  }

  case object CleanedPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ "cleaned"
  }

  case object PageWithCleanup extends QuestionPage[String] {
    override def path: JsPath = JsPath \ "withCleanup"

    override def cleanup(value: Option[String], userAnswers: UserAnswers): Try[UserAnswers] =
      userAnswers.remove(CleanedPage)
  }
}
