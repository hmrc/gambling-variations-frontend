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
import forms.BusinessChangeAddrScreenerFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.BusinessChangeAddrScreenerView

import scala.jdk.CollectionConverters.*

class BusinessChangeAddrScreenerViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[BusinessChangeAddrScreenerView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val formProvider = new BusinessChangeAddrScreenerFormProvider()
    private val form = formProvider()

    def docFor(isUkAddress: Boolean): Document = {
      val html = view(form, NormalMode, isUkAddress)(request, messages)
      Jsoup.parse(html.body)
    }
  }

  "BusinessChangeAddrScreenerView" - {

    "must render page correctly" in new Setup {
      val doc: Document = docFor(isUkAddress = true)

      doc.title must include(messages("businessChangeAddrScreener.title"))

      doc.title must include(messages("changeRegistrationDetails.caption"))

      doc.select("span").select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))

      doc.select("h1").text() must include(messages("businessChangeAddrScreener.heading"))

      doc.select("button.govuk-button").text must include(messages("site.continue"))
    }

    "when the business address is a UK address" - {

      "must render three options with an 'or' divider before the last option" in new Setup {
        val doc: Document = docFor(isUkAddress = true)

        val labels: Seq[String] = doc.select(".govuk-radios__item .govuk-label").asScala.map(_.text()).toSeq

        labels mustEqual Seq(
          messages("businessChangeAddrScreener.uk.differentUkAddress"),
          messages("businessChangeAddrScreener.uk.changeToNonUkAddress"),
          messages("businessChangeAddrScreener.editCurrentAddress")
        )

        doc.select(".govuk-radios__divider").text() mustEqual messages("site.or")
      }
    }

    "when the business address is not a UK address" - {

      "must render two options with no divider" in new Setup {
        val doc: Document = docFor(isUkAddress = false)

        val labels: Seq[String] = doc.select(".govuk-radios__item .govuk-label").asScala.map(_.text()).toSeq

        labels mustEqual Seq(
          messages("businessChangeAddrScreener.nonuk.changeToUkAddress"),
          messages("businessChangeAddrScreener.editCurrentAddress")
        )

        doc.select(".govuk-radios__divider") mustBe empty
      }
    }
  }
}
