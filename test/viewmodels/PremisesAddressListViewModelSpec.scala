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

package viewmodels

import base.SpecBase
import forms.licencespremises.PremisesAddressListFormProvider
import models.{NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import pages.licencespremises.{AddPremisesAddressPage, PremisesDetailsPage}
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.{Checkboxes, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import views.html.licencespremises.PremisesAddressListView

import java.time.LocalDate

class PremisesAddressListViewModelSpec extends SpecBase {

  trait Setup {
    val app = applicationBuilder().build()
    implicit val request: play.api.mvc.Request[?] = FakeRequest()
    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
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
                "address1"     -> "123 Road",
                "address2"     -> "Avenue",
                "address3"     -> "Hackney",
                "address4"     -> "London",
                "postcode"     -> "E8 1EA",
                "systemDate"   -> LocalDate.now()
              ),
              Json.obj(
                "mgdRegNumber" -> "XGM000001761",
                "address1"     -> "456 Road",
                "address2"     -> "Avenue",
                "address3"     -> "Hackney",
                "address4"     -> "London",
                "postcode"     -> "E8 2EA",
                "systemDate"   -> LocalDate.now()
              ),
              Json.obj(
                "mgdRegNumber" -> "XGM000001761",
                "address1"     -> "789 Road",
                "address2"     -> "Avenue",
                "address3"     -> "Hackney",
                "address4"     -> "London",
                "postcode"     -> "E8 3EA",
                "systemDate"   -> LocalDate.now()
              )
            )
          )
        )
      )
    )
    private val formProvider = new PremisesAddressListFormProvider()
    private val form = formProvider()
    private val preparedFormWithAnswers =
      userAnswers
        .get(AddPremisesAddressPage)
        .fold(form)(form.fill)
    private val maxPremisesNumber = 100
    private val addressList = userAnswers.get(PremisesDetailsPage).fold(Seq.empty)(list => list.premises)
    private val view = app.injector.instanceOf[PremisesAddressListView]
    private val formWithErrors = formProvider().bind(Map.empty[String, String])
    private val html = view(formWithErrors, NormalMode, addressList, maxPremisesNumber)(request, messages)
    val viewModel: SummaryList = PremisesAddressListViewModel.from(addressList)
    val doc: Document = Jsoup.parse(html.body)
  }

  "PremisesAddressList" - {
    "from" - {
      "should render correct view content from a SummaryList" in new Setup {
        viewModel.rows.head.key.classes      must include("govuk-!-display-none")
        viewModel.rows.head.value.classes    must include("govuk-!-width-one-half")
        viewModel.rows.head.value.toString   must include("123 Road, Avenue, Hackney, London, E8 1EA")
        viewModel.rows(1).value.toString     must include("456 Road, Avenue, Hackney, London, E8 2EA")
        viewModel.rows(2).value.toString     must include("789 Road, Avenue, Hackney, London, E8 3EA")
        viewModel.rows.head.actions.toString must include(messages("site.change"))
        viewModel.rows(1).actions.toString   must include(messages("site.change"))
        viewModel.rows(1).actions.toString   must include(messages("site.change"))
        viewModel.rows.head.actions.toString must include(messages("site.remove"))
        viewModel.rows(1).actions.toString   must include(messages("site.remove"))
        viewModel.rows(2).actions.toString   must include(messages("site.remove"))
      }

    }
  }
}
