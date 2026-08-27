package views

import base.SpecBase
import forms.PartnerDetailsIsBusinessIncorporatedFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.PartnerDetailsIsBusinessIncorporatedView

class PartnerDetailsIsBusinessIncorporatedViewSpec extends SpecBase {

  private val form = new PartnerDetailsIsBusinessIncorporatedFormProvider()()

  "PartnerDetailsIsBusinessIncorporatedView" - {

    "render the page correctly" in {

      val application = applicationBuilder().build()

      running(application) {

        val view = application.injector.instanceOf[PartnerDetailsIsBusinessIncorporatedView]

        val html = view(form, NormalMode)(FakeRequest(), messages(application))

        val document: Document = Jsoup.parse(html.toString)

        document.title() must include(
          messages(application)("partnerDetailsIsBusinessIncorporated.title")
        )

        document.select("h1").select(".govuk-fieldset__heading").text() mustEqual
          messages(application)("partnerDetailsIsBusinessIncorporated.heading")

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

        val view = application.injector.instanceOf[PartnerDetailsIsBusinessIncorporatedView]

        val boundForm = form.bind(Map("value" -> ""))

        val html = view(boundForm, NormalMode)(FakeRequest(), messages(application))

        val document: Document = Jsoup.parse(html.toString)

        document.select(".govuk-error-summary").size() mustEqual 1

        document.body().text() must include(
          messages(application)("partnerDetailsIsBusinessIncorporated.error.required")
        )
      }
    }
  }
}
