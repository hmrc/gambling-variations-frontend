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

package views

import base.SpecBase
import forms.SoleProprietorNameFormProvider
import models.{NormalMode, SoleProprietorName}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.ChangePartnerDetailsSoleProprietorNameView

import scala.jdk.CollectionConverters.*

class ChangePartnerDetailsSoleProprietorNameViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[ChangePartnerDetailsSoleProprietorNameView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val formProvider = new SoleProprietorNameFormProvider()
    val soleProprietorName = SoleProprietorName(
      title      = "Mr",
      firstName  = "Tom",
      middleName = Some("John"),
      lastName   = "Smith"
    )
    private val filledForm = formProvider().fill(soleProprietorName)

    private val html = view(filledForm, NormalMode)(request, messages)

    val doc: Document = Jsoup.parse(html.body)

  }

  "ChangePartnerDetailsSoleProprietorNameView" - {

    "must render page correctly" in new Setup {

      doc.title must include(messages("soleProprietorName.title"))

      doc.select("span").select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))

      val inputs: Map[String, Element] = doc.select("input").asScala.map(e => e.id -> e).toMap

      inputs
        .getOrElse("title", fail(s"Could not find input with id 'title'"))
        .`val`()
        .must(include(soleProprietorName.title))
      inputs
        .getOrElse("firstName", fail(s"Could not find input with id 'firstName'"))
        .`val`()
        .must(include(soleProprietorName.firstName))
      inputs
        .getOrElse("middleName", fail(s"Could not find input with id 'middleName'"))
        .`val`()
        .must(include(soleProprietorName.middleName.get))
      inputs
        .getOrElse("lastName", fail(s"Could not find input with id 'lastName'"))
        .`val`()
        .must(include(soleProprietorName.lastName))

      doc.body().text() must include(messages("soleProprietorName.title.label"))
      doc.body().text() must include(messages("soleProprietorName.firstName.label"))
      doc.body().text() must include(messages("soleProprietorName.middleName.label"))
      doc.body().text() must include(messages("soleProprietorName.lastName.label"))

      doc.select("button.govuk-button").text must include(messages("site.continue"))

    }

  }
}
