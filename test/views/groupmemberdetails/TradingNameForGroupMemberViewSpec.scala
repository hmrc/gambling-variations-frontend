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

package views.groupmemberdetails

import base.SpecBase
import controllers.groupmemberdetails.routes
import forms.groupmemberdetails.TradingNameForGroupMemberFormProvider
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.groupmemberdetails.TradingNameForGroupMemberView

class TradingNameForGroupMemberViewSpec extends SpecBase {

  "TradingNameForGroupMemberView" - {
    Seq(
      (false, "tradingNameForGroupMember", "Do you want to add a trading name for the group member?"),
      (true, "removeTradingNameForGroupMember", "Are you sure you want to remove this group member’s trading name?")
    ).foreach { case (remove, messageKey, heading) =>
      val form = new TradingNameForGroupMemberFormProvider()(messageKey)
      def submitCall = routes.TradingNameForGroupMemberController.onSubmit(remove)

      s"for $messageKey" - {
        "must render the heading, Back link, Yes and No options, and Continue button" in {
          val application = applicationBuilder().build()

          running(application) {
            val view = application.injector.instanceOf[TradingNameForGroupMemberView]
            val msgs = messages(application)
            val document = Jsoup.parse(view(form, messageKey, submitCall)(FakeRequest(), msgs).body)

            document.title() must include(heading)
            document.select("h1.govuk-fieldset__heading").text() mustEqual heading
            document.select("a.govuk-back-link").text() mustEqual msgs("site.back")
            document.select("a.govuk-back-link").attr("href") mustEqual "#"
            document.select(".govuk-caption-l").text() mustEqual msgs("changeRegistrationDetails.caption")

            document.getElementById("value_0").attr("value") mustEqual "yes"
            document.select("label[for=value_0]").text() mustEqual msgs("site.yes")
            document.getElementById("value_1").attr("value") mustEqual "no"
            document.select("label[for=value_1]").text() mustEqual msgs("site.no")

            document.select("button[type=submit]").text() mustEqual msgs("site.continue")
          }
        }

        "must link the validation summary to the first option" in {
          val application = applicationBuilder().build()

          running(application) {
            val view = application.injector.instanceOf[TradingNameForGroupMemberView]
            val boundForm = form.bind(Map.empty[String, String])
            val msgs = messages(application)
            val document = Jsoup.parse(view(boundForm, messageKey, submitCall)(FakeRequest(), msgs).body)

            document.title() must startWith(msgs("error.title.prefix"))
            document.select(".govuk-error-summary__list a").attr("href") mustEqual "#value_0"
            document.getElementById("value_0").attr("value") mustEqual "yes"
          }
        }
      }
    }
  }
}
