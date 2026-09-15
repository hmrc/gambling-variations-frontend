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
import forms.licencespremises.OtherLicencesAndPermitsNIFormProvider
import models.licencespremises.OtherLicencesAndPermitsNI.getSelectedLicencesAndPermits
import models.UserAnswers
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases.{Checkboxes, Text}

class OtherLicencesAndPermitsNIViewModelSpec extends SpecBase {

  trait Setup {
    val app = applicationBuilder().build()
    implicit val request: play.api.mvc.Request[?] = FakeRequest()
    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
    private val userAnswers = UserAnswers(
      "id",
      Json.obj(
        "licencesPremisesSection" -> Json.obj(
          "mgdRegNum"    -> "XGM000001761",
          "amusement"    -> "1",
          "bingo"        -> "0",
          "bookmaking"   -> "0",
          "serveAlcohol" -> "1",
          "regCert"      -> "0"
        )
      )
    )
    private val formProvider = new OtherLicencesAndPermitsNIFormProvider()
    private val form = formProvider()
    private val preparedForm = form.fill(getSelectedLicencesAndPermits(userAnswers))
    val viewModel: Checkboxes = OtherLicencesAndPermitsNIViewModel(preparedForm)

  }

  "OtherLicencesAndPermitsNIViewModel" - {
    "apply" - {
      "should render correct view content" in new Setup {
        val index = 1
        viewModel.items.head.content mustEqual Text(messages("otherLicencesAndPermitsNI.option.amusement"))
        viewModel.items(1).content mustEqual Text(messages("otherLicencesAndPermitsNI.option.bingo"))
        viewModel.items(2).content mustEqual Text(messages("otherLicencesAndPermitsNI.option.bookmaking"))
        viewModel.items(3).content mustEqual Text(messages("otherLicencesAndPermitsNI.option.serveAlcohol"))
        viewModel.items(4).content mustEqual Text(messages("otherLicencesAndPermitsNI.option.regCert"))
        viewModel.items(5).divider mustBe Some(messages("site.or"))
        viewModel.items(6).content mustEqual Text(messages("otherLicencesAndPermitsNI.option.none"))
      }

      "should render correct names and ids" in new Setup {

        viewModel.name mustEqual "permitsNI[]"

        viewModel.items.head.id mustBe Some("permitsNI-amusement")
        viewModel.items(1).id mustBe Some("permitsNI-bingo")
        viewModel.items(2).id mustBe Some("permitsNI-bookmaking")
        viewModel.items(3).id mustBe Some("permitsNI-serveAlcohol")
        viewModel.items(4).id mustBe Some("permitsNI-regCert")
        viewModel.items(6).id mustBe Some("permitsNI-noOtherLicencesAndPremisesNISelected")
      }
    }
  }
}
