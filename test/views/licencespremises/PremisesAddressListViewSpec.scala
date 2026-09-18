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
import forms.licencespremises.PremisesAddressListFormProvider
import models.{NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import pages.licencespremises.{AddPremisesAddressPage, PremisesDetailsPage}
import play.api.i18n.Messages
import play.api.libs.json.{JsArray, Json}
import play.api.test.FakeRequest
import views.html.licencespremises.PremisesAddressListView

import java.time.LocalDate

class PremisesAddressListViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[PremisesAddressListView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val userAnswers = UserAnswers(
      id = userAnswersId,
      data = Json.obj(
        "licencesPremisesSection" -> Json.obj(
          "mgdRegNum" -> "XGM000001761",
          "premisesDetails" -> Json.obj(
            "totalRows" -> 1000,
            "premises" -> Json.arr(
              Json.obj(
                "mgdRegNumber" -> "XGM000001761",
                "address1" -> "123 Road",
                "address2" -> "Avenue",
                "address3" -> "London",
                "postcode" -> "E8 1EA",
                "systemDate" -> LocalDate.now()
              ),
              Json.obj(
                "mgdRegNumber" -> "XGM000001761",
                "address1" -> "456 Road",
                "address2" -> "Avenue",
                "address3" -> "London",
                "postcode" -> "E8 2EA",
                "systemDate" -> LocalDate.now()
              ),
              Json.obj(
                "mgdRegNumber" -> "XGM000001761",
                "address1" -> "789 Road",
                "address2" -> "Avenue",
                "address3" -> "London",
                "postcode" -> "E8 3EA",
                "systemDate" -> LocalDate.now()
              )
            )
          )
        )
      )
    )
    private val oneHundredUserAnswers = UserAnswers(
      id = userAnswersId,
      data = Json.obj(
        "licencesPremisesSection" -> Json.obj(
          "mgdRegNum" -> "XGM000001761",
          "premisesDetails" -> Json.obj(
            "totalRows" -> 1000,
            "premises" -> Json.toJson(Seq.fill(100)(
              Json.obj(
                "mgdRegNumber" -> "XGM000001761",
                "address1" -> "123 Road",
                "address2" -> "Avenue",
                "address3" -> "London",
                "postcode" -> "E8 1EA",
                "systemDate" -> LocalDate.now()
              ))).as[JsArray]
          )
        )
      )
    )
    private val formProvider = new PremisesAddressListFormProvider()
    private val form = formProvider()
    private val formWithErrors = form.bind(Map.empty[String, String])

    private val addressList = userAnswers.get(PremisesDetailsPage).fold(Seq.empty)(list => list.premises)
    private val addressListOneHundred = oneHundredUserAnswers.get(PremisesDetailsPage).fold(Seq.empty)(list => list.premises)

    private val maxPremises = 100

    private val html = view(form, NormalMode, addressList, maxPremises)(request, messages)
    private val htmlWith100 = view(form, NormalMode, addressListOneHundred, maxPremises)(request, messages)
    private val htmlWithError = view(formWithErrors, NormalMode, addressList, maxPremises)(request, messages)

    val doc: Document = Jsoup.parse(html.body)
    val doc100: Document = Jsoup.parse(htmlWith100.body)
    val docWithFormErrors: Document = Jsoup.parse(htmlWithError.body)
  }

  "OtherLicencesAndPermitsGBView" - {

    "must render page correctly for under 100 records" in new Setup {

      doc.title must include(messages("premisesAddressList.title"))
      doc.title must include(messages("changeRegistrationDetails.caption"))
      doc.select("h1").text() mustEqual messages("premisesAddressList.heading")
      doc.select(".govuk-hint").text mustEqual messages("premisesAddressList.hint")
      doc.text must include(messages("premisesAddressList.question.add"))
      doc.text must include(messages("premisesAddressList.message.submit"))
      doc.text must include("Showing 1 to 3 of 3 records")
      doc.text must include(messages("premisesAddressList.yesLabel"))
      doc.text must include(messages("premisesAddressList.noLabel"))
      doc.select(".govuk-hint").text mustEqual messages("premisesAddressList.hint")
      doc.select("button.govuk-button").text must include(messages("site.continue"))
    }

    "must render page correctly for 100 records" in new Setup {
      doc100.text must include("Showing 1 to 100 of 100 records")
      doc100.text must include(messages("premisesAddressList.max"))
      doc100.text must not include(messages("premisesAddressList.question.add"))
      doc100.text must not include(messages("premisesAddressList.hint"))
      doc100.text must not include(messages("premisesAddressList.yesLabel"))
      doc100.text must not include(messages("premisesAddressList.noLabel"))

    }

    "must show error message when form has errors" in new Setup {
      docWithFormErrors.text must include(messages("premisesAddressList.error.required"))
    }
  }
}
