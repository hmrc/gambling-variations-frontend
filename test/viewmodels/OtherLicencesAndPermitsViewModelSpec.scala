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
import forms.licencespremises.OtherLicencesAndPermitsGBFormProvider
import models.licencespremises.OtherLicencesAndPermitsGB.getSelectedLicencesAndPermits
import models.UserAnswers
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases
import uk.gov.hmrc.govukfrontend.views.Aliases.{Checkboxes, Text}

class OtherLicencesAndPermitsViewModelSpec extends SpecBase {

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
          "mgdRegNum"           -> "XGM000001761",
          "clubGaming"          -> "1",
          "clubMachine"         -> "0",
          "clubPremises"        -> "0",
          "familyEntertainment" -> "1",
          "localAuthority"      -> "0",
          "onPremises"          -> "1",
          "prizeGaming"         -> "0"
        )
      )
    )
    private val formProvider = new OtherLicencesAndPermitsGBFormProvider()
    private val form = formProvider()
    private val preparedForm = form.fill(getSelectedLicencesAndPermits(userAnswers))
    val viewModel: Checkboxes = OtherLicencesAndPermitsViewModel(preparedForm)

  }

  "OtherLicencesAndPermitsViewModel" - {
    "apply" - {
      "should render correct view content" in new Setup {
        val index = 1
        viewModel.items.head.content mustEqual Text(messages("otherLicencesAndPermitsGB.option.clubGaming"))
        viewModel.items(1).content mustEqual Text(messages("otherLicencesAndPermitsGB.option.clubMachine"))
        viewModel.items(2).content mustEqual Text(messages("otherLicencesAndPermitsGB.option.clubPremises"))
        viewModel.items(3).content mustEqual Text(messages("otherLicencesAndPermitsGB.option.familyEntertainment"))
        viewModel.items(4).content mustEqual Text(messages("otherLicencesAndPermitsGB.option.localAuthority"))
        viewModel.items(5).content mustEqual Text(messages("otherLicencesAndPermitsGB.option.onPremises"))
        viewModel.items(6).content mustEqual Text(messages("otherLicencesAndPermitsGB.option.prizeGaming"))
        viewModel.items(7).divider mustBe Some(messages("site.or"))
        viewModel.items(8).content mustEqual Text(messages("otherLicencesAndPermitsGB.option.none"))
      }

      "should render correct names and ids" in new Setup {

        viewModel.name mustEqual "permitsGB"

        viewModel.items.head.id mustBe Some("permitsGB-clubGaming")
        viewModel.items(1).id mustBe Some("permitsGB-clubMachine")
        viewModel.items(2).id mustBe Some("permitsGB-clubPremises")
        viewModel.items(3).id mustBe Some("permitsGB-familyEntertainment")
        viewModel.items(4).id mustBe Some("permitsGB-localAuthority")
        viewModel.items(5).id mustBe Some("permitsGB-onPremises")
        viewModel.items(6).id mustBe Some("permitsGB-prizeGaming")
        viewModel.items(8).id mustBe Some("permitsGB-none")
      }
    }
  }
}
