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

package views.licencespremises

import base.SpecBase
import forms.licencespremises.OtherLicencesAndPermitsGBFormProvider
import models.{NormalMode, UserAnswers}
import models.licencespremises.OtherLicencesAndPermitsGB.{getSelectedLicencesAndPermits, mappedValuesWithPages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import viewmodels.OtherLicencesAndPermitsViewModel
import views.html.licencespremises.OtherLicencesAndPermitsGBView

class OtherLicencesAndPermitsGBViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[OtherLicencesAndPermitsGBView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val formProvider = new OtherLicencesAndPermitsGBFormProvider()
    private val ua = UserAnswers(
      userAnswersId,
      Json.obj(
        "licencesPremisesSection" -> Json.obj(
          "mgdRegNum"           -> "XGM000001761",
          "clubGaming"          -> "1",
          "clubMachine"         -> "0",
          "clubPremises"        -> "1",
          "familyEntertainment" -> "0",
          "localAuthority"      -> "1",
          "onPremises"          -> "0",
          "prizeGaming"         -> "1"
        )
      )
    )

    private val form = formProvider()
    private val preparedForm = form.fill(getSelectedLicencesAndPermits(ua))
    private val html = view(form, NormalMode, OtherLicencesAndPermitsViewModel(preparedForm))(request, messages)

    val doc: Document = Jsoup.parse(html.body)
  }

  "OtherLicencesAndPermitsGBView" - {

    "must render page correctly" in new Setup {

      doc.title must include(messages("otherLicencesAndPermitsGB.title"))
      doc.title must include(messages("changeRegistrationDetails.caption"))
      doc.select("h1").text() mustEqual messages("otherLicencesAndPermitsGB.heading")
      doc.select(".govuk-hint").text mustEqual messages("otherLicencesAndPermitsGB.hint")
      doc.select("label[for=permitsGB-clubGaming]").text mustEqual messages("otherLicencesAndPermitsGB.option.clubGaming")
      doc.select("label[for=permitsGB-clubMachine]").text mustEqual messages("otherLicencesAndPermitsGB.option.clubMachine")
      doc.select("label[for=permitsGB-clubPremises]").text mustEqual messages("otherLicencesAndPermitsGB.option.clubPremises")
      doc.select("label[for=permitsGB-familyEntertainment]").text mustEqual messages("otherLicencesAndPermitsGB.option.familyEntertainment")
      doc.select("label[for=permitsGB-localAuthority]").text mustEqual messages("otherLicencesAndPermitsGB.option.localAuthority")
      doc.select("label[for=permitsGB-onPremises]").text mustEqual messages("otherLicencesAndPermitsGB.option.onPremises")
      doc.select("label[for=permitsGB-prizeGaming]").text mustEqual messages("otherLicencesAndPermitsGB.option.prizeGaming")
      doc.select("label[for=permitsGB-none]").text mustEqual messages("otherLicencesAndPermitsGB.option.none")
      doc.select("button.govuk-button").text must include(messages("site.continue"))
    }

    "must pre-populate fields if data already exists" in new Setup {
      doc.select("input[value=clubGaming]").hasAttr("checked") mustBe true
      doc.select("input[value=clubMachine]").hasAttr("checked") mustBe false
      doc.select("input[value=clubPremises]").hasAttr("checked") mustBe true
      doc.select("input[value=familyEntertainment]").hasAttr("checked") mustBe false
      doc.select("input[value=localAuthority]").hasAttr("checked") mustBe true
      doc.select("input[value=onPremises]").hasAttr("checked") mustBe false
      doc.select("input[value=prizeGaming]").hasAttr("checked") mustBe true
    }

  }
}
