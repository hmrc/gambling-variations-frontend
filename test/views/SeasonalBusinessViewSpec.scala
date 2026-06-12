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
import forms.SeasonalBusinessFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.SeasonalBusinessView

class SeasonalBusinessViewSpec extends SpecBase {

  "SeasonalBusinessView" - {

    "must render the page with the expected content" in new Setup {

      val html = view(form, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("seasonalBusiness.title"))
      doc.select("h1").text mustEqual messages("seasonalBusiness.heading")
      doc.select(".govuk-hint").text mustEqual messages("seasonalBusiness.paragraph")

      doc.select("form").attr("action") mustEqual controllers.routes.SeasonalBusinessController.onSubmit(NormalMode).url

      doc.select("input[type=radio]").size mustEqual 2
      doc.select("input#isSeasonalBusiness").attr("name") mustEqual "isSeasonalBusiness"
      doc.select("label[for=isSeasonalBusiness]").text mustEqual messages("site.yes")
      doc.select("input#isSeasonalBusiness-no").attr("name") mustEqual "isSeasonalBusiness"
      doc.select("label[for=isSeasonalBusiness-no]").text mustEqual messages("site.no")

      doc.select("button").text must include(messages("site.continue"))
    }

    "must render an error summary when the form has errors" in new Setup {

      val errorForm = form.bind(Map("isSeasonalBusiness" -> ""))

      val html = view(errorForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").size mustBe 1
    }
  }

  trait Setup {
    val app = applicationBuilder().build()
    val form = new SeasonalBusinessFormProvider()()
    val view = app.injector.instanceOf[SeasonalBusinessView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}
