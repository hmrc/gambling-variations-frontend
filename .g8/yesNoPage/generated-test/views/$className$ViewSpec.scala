package views

import base.SpecBase
import forms.$className$FormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.$className$View

class $className$ViewSpec extends SpecBase {

  private val form = new $className$FormProvider()()

  "$className$View" - {

    "render the page correctly" in {

      val application = applicationBuilder().build()

      running(application) {

        val view = application.injector.instanceOf[$className$View]

        val html = view(form, NormalMode)(FakeRequest(), messages(application))

        val document: Document = Jsoup.parse(html.toString)

        document.title() must include(
          messages(application)("$className;format="decap"$.title")
        )

        document.select("h1").select(".govuk-fieldset__heading").text() mustEqual
          messages(application)("$className;format="decap"$.heading")

        document.select("span").select(".govuk-caption-l").text() mustEqual
          messages(application)("changeRegistrationDetails.caption")

        document.getElementById("value").attr("value") mustEqual "true"

        document.getElementById("value-no").attr("value") mustEqual "false"

        document.select(".govuk-button").text() mustEqual
          messages(application)("site.continue")
      }
    }

    "render an error summary when there are form errors" in {

      val application = applicationBuilder().build()

      running(application) {

        val view = application.injector.instanceOf[$className$View]

        val boundForm = form.bind(Map("value" -> ""))

        val html = view(boundForm, NormalMode)(FakeRequest(), messages(application))

        val document: Document = Jsoup.parse(html.toString)

        document.select(".govuk-error-summary").size() mustEqual 1

        document.body().text() must include(
          messages(application)("$className;format="decap"$.error.required")
        )
      }
    }
  }
}
