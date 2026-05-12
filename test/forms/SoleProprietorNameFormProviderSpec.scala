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

package forms

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class SoleProprietorNameFormProviderSpec extends StringFieldBehaviours {

  val form = new SoleProprietorNameFormProvider()()
  private val validTitles = Gen.oneOf("Mr", "Mrs", "Ms", "Mx", "O'Neil-Smith")
  private val validNames = Gen.oneOf("John", "Jane-Ann", "O'Connor", "A1ex", "Mary 2")

  ".title" - {

    val fieldName = "title"
    val requiredKey = "soleProprietorName.error.title.required"
    val lengthKey = "soleProprietorName.error.title.length"
    val invalidKey = "soleProprietorName.error.title.invalid"
    val maxLength = 20

    behave like fieldThatBindsValidData(form, fieldName, validTitles)

    s"not bind strings longer than $maxLength characters" in {
      val result = form.bind(Map(fieldName -> ("A" * (maxLength + 1)))).apply(fieldName)
      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }

    behave like mandatoryField(
      form,
      fieldName,
      FormError(fieldName, requiredKey)
    )

    "not bind invalid characters" in {
      val result = form.bind(Map(fieldName -> "Mr.")).apply(fieldName)
      result.errors must contain only FormError(fieldName, invalidKey, Seq("^[A-Za-z' -]+$"))
    }

    "bind length and invalid character errors together" in {
      val result = form.bind(Map(fieldName -> (("x" * 21) + "!"))).apply(fieldName)
      result.errors mustBe Seq(
        FormError(fieldName, lengthKey, Seq(maxLength)),
        FormError(fieldName, invalidKey, Seq("^[A-Za-z' -]+$"))
      )
    }
  }

  ".firstName" - {

    val fieldName = "firstName"
    val requiredKey = "soleProprietorName.error.firstName.required"
    val lengthKey = "soleProprietorName.error.firstName.length"
    val invalidKey = "soleProprietorName.error.firstName.invalid"
    val maxLength = 100

    behave like fieldThatBindsValidData(form, fieldName, validNames)

    s"not bind strings longer than $maxLength characters" in {
      val result = form.bind(Map(fieldName -> ("A" * (maxLength + 1)))).apply(fieldName)
      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }

    behave like mandatoryField(
      form,
      fieldName,
      FormError(fieldName, requiredKey)
    )

    "not bind invalid characters" in {
      val result = form.bind(Map(fieldName -> "John@")).apply(fieldName)
      result.errors must contain only FormError(fieldName, invalidKey, Seq("^[A-Za-z0-9' -]+$"))
    }

    "bind length and invalid character errors together" in {
      val result = form.bind(Map(fieldName -> (("a" * 101) + "@"))).apply(fieldName)
      result.errors mustBe Seq(
        FormError(fieldName, lengthKey, Seq(maxLength)),
        FormError(fieldName, invalidKey, Seq("^[A-Za-z0-9' -]+$"))
      )
    }
  }

  ".middleName" - {

    val fieldName = "middleName"
    val lengthKey = "soleProprietorName.error.middleName.length"
    val invalidKey = "soleProprietorName.error.middleName.invalid"
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validNames
    )

    s"not bind strings longer than $maxLength characters" in {
      val result = form.bind(Map(fieldName -> ("A" * (maxLength + 1)))).apply(fieldName)
      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }

    "bind None when field is missing" in {
      val result = form.bind(Map.empty[String, String]).value
      result.flatMap(_.middleName) mustBe None
    }

    "bind None when field is empty" in {
      val result = form.bind(Map(fieldName -> "")).value
      result.flatMap(_.middleName) mustBe None
    }

    "bind None when field is whitespace" in {
      val result = form.bind(Map(fieldName -> "   ")).value
      result.flatMap(_.middleName) mustBe None
    }

    "bind Some when valid value provided" in {
      val middleName = "Michael"

      val result = form
        .bind(
          Map(
            "title"     -> "Mr",
            "firstName" -> "John",
            "lastName"  -> "Doe",
            fieldName   -> middleName
          )
        )
        .value

      result.flatMap(_.middleName) mustBe Some(middleName)
    }

    "not bind invalid characters" in {
      val result = form.bind(Map(fieldName -> "Middle!")).apply(fieldName)
      result.errors must contain only FormError(fieldName, invalidKey, Seq("^$|^[A-Za-z0-9' -]+$"))
    }

    "bind length and invalid character errors together" in {
      val result = form.bind(Map(fieldName -> (("a" * 101) + "!"))).apply(fieldName)
      result.errors mustBe Seq(
        FormError(fieldName, lengthKey, Seq(maxLength)),
        FormError(fieldName, invalidKey, Seq("^$|^[A-Za-z0-9' -]+$"))
      )
    }
  }

  ".lastName" - {

    val fieldName = "lastName"
    val requiredKey = "soleProprietorName.error.lastName.required"
    val lengthKey = "soleProprietorName.error.lastName.length"
    val invalidKey = "soleProprietorName.error.lastName.invalid"
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validNames
    )

    s"not bind strings longer than $maxLength characters" in {
      val result = form.bind(Map(fieldName -> ("A" * (maxLength + 1)))).apply(fieldName)
      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }

    behave like mandatoryField(
      form,
      fieldName,
      FormError(fieldName, requiredKey)
    )

    "not bind invalid characters" in {
      val result = form.bind(Map(fieldName -> "Smith!")).apply(fieldName)
      result.errors must contain only FormError(fieldName, invalidKey, Seq("^[A-Za-z0-9' -]+$"))
    }

    "bind length and invalid character errors together" in {
      val result = form.bind(Map(fieldName -> (("a" * 101) + "!"))).apply(fieldName)
      result.errors mustBe Seq(
        FormError(fieldName, lengthKey, Seq(maxLength)),
        FormError(fieldName, invalidKey, Seq("^[A-Za-z0-9' -]+$"))
      )
    }

    "trim input" in {

      val lastName = "  Smith  "

      val result = form
        .bind(
          Map(
            "title"     -> "Mr",
            "firstName" -> "John",
            "lastName"  -> "Doe",
            fieldName   -> lastName
          )
        )
        .value

      result.value.lastName mustBe "Smith"
    }

  }
}
