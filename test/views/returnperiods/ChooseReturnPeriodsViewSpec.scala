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

package views.returnperiods

import base.SpecBase
import forms.returnperiods.ChooseReturnPeriodsFormProvider
import models.{NormalMode, ReturnPeriodsVariant}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.returnperiods.ChooseReturnPeriodsView

class ChooseReturnPeriodsViewSpec extends SpecBase {

  private val formProvider =
    new ChooseReturnPeriodsFormProvider()

  "ChooseReturnPeriodsView" - {

    "render the standard variant correctly" in {

      val application = applicationBuilder().build()

      running(application) {

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        val html =
          view(
            form,
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            FakeRequest(),
            messages(application)
          )

        val document: Document =
          Jsoup.parse(html.toString)

        document.title() must include(
          messages(application)(
            s"${ReturnPeriodsVariant.Standard.messagePrefix}.title"
          )
        )

        document.title() must include(
          messages(application)(
            "changeRegistrationDetails.caption"
          )
        )

        document.body().text() must include(
          messages(application)(
            s"${ReturnPeriodsVariant.Standard.messagePrefix}.heading"
          )
        )

        document.body().text() must include(
          messages(application)("site.continue")
        )

        document.body().text() must include(
          messages(application)("chooseReturnPeriods.p")
        )

        document.select("input[type=radio]").size() mustEqual 3
      }
    }

    "render the non-standard variant correctly" in {

      val application = applicationBuilder().build()

      running(application) {

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.NonStandard.errorMessageKey
          )

        val html =
          view(
            form,
            NormalMode,
            ReturnPeriodsVariant.NonStandard
          )(
            FakeRequest(),
            messages(application)
          )

        val document: Document =
          Jsoup.parse(html.toString)

        document.title() must include(
          messages(application)(
            s"${ReturnPeriodsVariant.NonStandard.messagePrefix}.title"
          )
        )

        document.title() must include(
          messages(application)(
            "changeRegistrationDetails.caption"
          )
        )

        document.body().text() must include(
          messages(application)(
            s"${ReturnPeriodsVariant.NonStandard.messagePrefix}.heading"
          )
        )

        document.body().text() must include(
          messages(application)("site.continue")
        )

        document.select("input[type=radio]").size() mustEqual 3
      }
    }

    "render all return period options" in {

      val application = applicationBuilder().build()

      running(application) {

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        val html =
          view(
            form,
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            FakeRequest(),
            messages(application)
          )

        val document: Document =
          Jsoup.parse(html.toString)

        val radioInputs =
          document.select("input[type=radio]")

        radioInputs.size() mustEqual 3

        radioInputs.get(0).attr("value") mustEqual "jan"
        radioInputs.get(1).attr("value") mustEqual "feb"
        radioInputs.get(2).attr("value") mustEqual "mar"
      }
    }

    "render an error summary when there are form errors" in {

      val application = applicationBuilder().build()

      running(application) {

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        val boundForm =
          form.bind(
            Map("value" -> "")
          )

        val html =
          view(
            boundForm,
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            FakeRequest(),
            messages(application)
          )

        val document: Document =
          Jsoup.parse(html.toString)

        document
          .select(".govuk-error-summary")
          .size() mustEqual 1

        document.body().text() must include(
          messages(application)(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )
        )
      }
    }

    "not render the error summary when the form has no errors" in {

      val application = applicationBuilder().build()

      running(application) {

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        val html =
          view(
            form,
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            FakeRequest(),
            messages(application)
          )

        val document =
          Jsoup.parse(html.toString)

        document
          .select(".govuk-error-summary")
          .size() mustEqual 0
      }
    }

    "render the standard variant explanatory paragraph" in {

      val application = applicationBuilder().build()

      running(application) {

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        val html =
          view(
            form,
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            FakeRequest(),
            messages(application)
          )

        val document =
          Jsoup.parse(html.toString)

        document.body().text() must include(
          messages(application)("chooseReturnPeriods.p")
        )
      }
    }

    "not render the explanatory paragraph for the non-standard variant" in {

      val application = applicationBuilder().build()

      running(application) {

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.NonStandard.errorMessageKey
          )

        val html =
          view(
            form,
            NormalMode,
            ReturnPeriodsVariant.NonStandard
          )(
            FakeRequest(),
            messages(application)
          )

        val document =
          Jsoup.parse(html.toString)

        document.body().text() must not include (
          messages(application)("chooseReturnPeriods.p")
        )
      }
    }

    "render the correct form action for NormalMode" in {

      val application = applicationBuilder().build()

      running(application) {

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        val html =
          view(
            form,
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            FakeRequest(),
            messages(application)
          )

        val document =
          Jsoup.parse(html.toString)

        document
          .select("form")
          .attr("action") mustEqual
          controllers.returnperiods.routes.ChooseReturnPeriodsController
            .onSubmit(NormalMode)
            .url
      }
    }
  }
}
