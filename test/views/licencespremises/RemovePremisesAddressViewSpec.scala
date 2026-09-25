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
import forms.licencespremises.RemovePremisesAddressFormProvider
import models.{Address, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import utils.AddressFormatter
import views.html.licencespremises.RemovePremisesAddressView

class RemovePremisesAddressViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[RemovePremisesAddressView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val formProvider = new RemovePremisesAddressFormProvider()
    private val form = formProvider()

    val chosenAddress: Address = Address(
      address1 = "Flat 1",
      address2 = Some("10 Market Calle"),
      address3 = Some("Madrid"),
      address4 = None,
      postcode = Some("28085"),
      country  = None
    )

    private val html = view(form, NormalMode, chosenAddress)(request, messages)

    val doc: Document = Jsoup.parse(html.body)

  }

  "RemovePremisesAddressView" - {

    "must render page correctly" in new Setup {

      doc.title must include(messages("removePremisesAddress.title"))

      doc.select("span").select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))
      doc.select(".govuk-hint").text()                     must include("Flat 1 10 Market Calle")

      val legend: Elements = doc.select("legend.govuk-fieldset__legend")
      legend.text must include(messages("removePremisesAddress.heading"))

      doc.select("button.govuk-button").text must include(messages("site.continue"))

    }

    "must render the premises address" in new Setup {

      val addressLines: Seq[String] = AddressFormatter.format(chosenAddress)

      val renderedAddress: String = doc
        .select(".govuk-hint")
        .text()

      addressLines.foreach { line =>
        renderedAddress must include(line)
      }

    }
  }
}
