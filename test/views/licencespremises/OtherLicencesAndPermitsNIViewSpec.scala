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
import forms.licencespremises.OtherLicencesAndPermitsNIFormProvider
import models.{NormalMode, UserAnswers}
import models.licencespremises.OtherLicencesAndPermitsNI.{getSelectedLicencesAndPermits, mappedValuesWithPages}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import viewmodels.OtherLicencesAndPermitsNIViewModel
import views.html.licencespremises.OtherLicencesAndPermitsNIView

class OtherLicencesAndPermitsNIViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[OtherLicencesAndPermitsNIView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val formProvider = new OtherLicencesAndPermitsNIFormProvider()
    private val ua = UserAnswers(
      userAnswersId,
      Json.obj(
        "licencesPremisesSection" -> Json.obj(
          "mgdRegNum"                            -> "XGM000001761",
          "amusement"                            -> "1",
          "bingo"                                -> "0",
          "bookmaking"                           -> "1",
          "serveAlcohol"                         -> "0",
          "regCert"                              -> "1",
          "noOtherLicencesAndPremisesNISelected" -> "0"
        )
      )
    )

    private val form = formProvider()
    private val preparedForm = form.fill(getSelectedLicencesAndPermits(ua))
    private val html = view(form, NormalMode, OtherLicencesAndPermitsNIViewModel(preparedForm))(request, messages)

    val doc: Document = Jsoup.parse(html.body)
  }

  "OtherLicencesAndPermitsNIView" - {

    "must render page correctly" in new Setup {

      doc.title must include(messages("otherLicencesAndPermitsNI.title"))
      doc.title must include(messages("changeRegistrationDetails.caption"))
      doc.select("h1").text() mustEqual messages("otherLicencesAndPermitsNI.heading")
      doc.select(".govuk-hint").text mustEqual messages("otherLicencesAndPermitsNI.hint")
      doc.select("label[for=permitsNI-amusement]").text mustEqual messages("otherLicencesAndPermitsNI.option.amusement")
      doc.select("label[for=permitsNI-bingo]").text mustEqual messages("otherLicencesAndPermitsNI.option.bingo")
      doc.select("label[for=permitsNI-bookmaking]").text mustEqual messages("otherLicencesAndPermitsNI.option.bookmaking")
      doc.select("label[for=permitsNI-serveAlcohol]").text mustEqual messages("otherLicencesAndPermitsNI.option.serveAlcohol")
      doc.select("label[for=permitsNI-regCert]").text mustEqual messages("otherLicencesAndPermitsNI.option.regCert")
      doc.select("label[for=permitsNI-noOtherLicencesAndPremisesNISelected]").text mustEqual messages("otherLicencesAndPermitsNI.option.none")
      doc.select("button.govuk-button").text must include(messages("site.continue"))
    }

    "must pre-populate fields if data already exists" in new Setup {
      doc.select("input[value=amusement]").hasAttr("checked") mustBe true
      doc.select("input[value=bingo]").hasAttr("checked") mustBe false
      doc.select("input[value=bookmaking]").hasAttr("checked") mustBe true
      doc.select("input[value=serveAlcohol]").hasAttr("checked") mustBe false
      doc.select("input[value=regCert]").hasAttr("checked") mustBe true
      doc.select("input[value=noOtherLicencesAndPremisesNISelected]").hasAttr("checked") mustBe false
    }

  }
}
