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
import controllers.routes
import forms.LicencesPremisesFormProvider
import models.LicencesPremises
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.LicencesPremisesView

import scala.jdk.CollectionConverters.*

class LicencesPremisesViewSpec extends SpecBase {

  private val form = new LicencesPremisesFormProvider()()

  "LicencesPremisesView" - {

    "must render the premises address options, default back link and unique test locators" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest()
        val view = application.injector.instanceOf[LicencesPremisesView]
        val document: Document = Jsoup.parse(view(form)(request, messages(application)).toString)

        document.title() must include(messages(application)("licencesPremises.title"))
        document.select("h1.govuk-fieldset__heading").text() mustEqual
          messages(application)("licencesPremises.heading")

        LicencesPremises.values.zipWithIndex.foreach { case (option, index) =>
          val input = document.getElementById(s"value_$index")

          input.attr("value") mustEqual option.toString
          input.attr("data-testid") mustEqual s"licences-premises-${option.toString}"
          document.select(s"label[for=value_$index]").text() mustEqual
            messages(application)(s"licencesPremises.${option.toString}")
        }

        document.select("form[data-testid=licences-premises-form]").attr("action") mustEqual
          routes.LicencesPremisesController.onSubmit().url
        document.select(".govuk-radios").attr("data-testid") mustEqual "licences-premises-options"
        document.select("button.govuk-button").text() mustEqual messages(application)("site.continue")
        document.select("button.govuk-button").attr("data-testid") mustEqual "licences-premises-continue"

        val backLink = document.select("a.govuk-back-link")
        backLink.attr("href") mustEqual "#"

        val testIds = document.select("[data-testid]").asScala.map(_.attr("data-testid"))
        testIds.distinct must have size testIds.size
      }
    }

    "must render the required error against the first radio option" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest()
        val view = application.injector.instanceOf[LicencesPremisesView]
        val boundForm = form.bind(Map.empty[String, String])
        val document = Jsoup.parse(view(boundForm)(request, messages(application)).toString)
        val expectedError = messages(application)("licencesPremises.error.required")

        document.select(".govuk-error-summary").attr("data-testid") mustEqual
          "licences-premises-error-summary"
        document.select(".govuk-error-summary__list a").text() mustEqual expectedError
        document.select(".govuk-error-summary__list a").attr("href") mustEqual "#value_0"
        document.select(".govuk-error-message").text() must include(expectedError)
      }
    }
  }
}
