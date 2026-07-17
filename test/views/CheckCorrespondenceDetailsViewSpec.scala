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
import models.{Address, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import viewmodels.CheckCorrespondenceDetailsViewModel
import views.html.CheckCorrespondenceDetailsView
import controllers.routes

import scala.jdk.CollectionConverters.*

class CheckCorrespondenceDetailsViewSpec extends SpecBase with OptionValues {

  trait Setup {

    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[CheckCorrespondenceDetailsView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    val viewModel =
      CheckCorrespondenceDetailsViewModel(
        correspondenceName              = Some("Test Name"),
        addCorrespondenceAdditionalName = Some(true),
        additionalCorrespondenceName    = Some("Additional Name"),
        correspondenceAddress = Some(
          Address(
            address1 = "Line 1",
            address2 = Some("Line 2"),
            address3 = None,
            address4 = None,
            postcode = Some("AA1 1AA"),
            country  = None
          )
        ),
        addCorrespondenceAdditionalInformation = Some(true),
        correspondenceAdditionalInformation    = Some("Additional Information"),
        phoneNumber                            = Some("01234567890"),
        mobilePhoneNumber                      = Some("07123456789"),
        addCorrespondenceFaxNumber             = Some(true),
        faxNumber                              = Some("02000000000"),
        addCorrespondenceEmailAddress          = Some(true),
        emailAddress                           = Some("test@test.com"),
        isSubmitted                            = false
      )
  }

  "CheckCorrespondenceDetailsView" - {

    "must render page title and heading" in new Setup {

      val html = view(viewModel)

      val doc = Jsoup.parse(html.body)

      doc.title must include(
        messages("checkCorrespondenceDetails.title")
      )

      doc.select("h1").text mustEqual
        messages("checkCorrespondenceDetails.heading")
    }

    "must render correspondence details" in new Setup {

      val html = view(viewModel)

      val doc = Jsoup.parse(html.body)

      doc.text must include("Test Name")
      doc.text must include("Additional Name")
      doc.text must include("Additional Information")
      doc.text must include("01234567890")
      doc.text must include("07123456789")
      doc.text must include("02000000000")
      doc.text must include("test@test.com")
    }

    "must render continue button" in new Setup {

      val html = view(viewModel)

      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-button").text mustEqual
        messages("site.continue")
    }

    "must render remove correspondence details link" in new Setup {

      val html = view(viewModel)

      val doc = Jsoup.parse(html.body)

      doc.text must include(
        messages("checkCorrespondenceDetails.message.remove")
      )

      doc
        .select("a[href]")
        .eachAttr("href")
        .asScala must contain(
        routes.RemoveCorrespondenceDetailsYesNoController
          .onPageLoad(NormalMode)
          .url
      )
    }

    "must display required to submit message when submitted" in new Setup {

      val submittedViewModel =
        viewModel.copy(
          isSubmitted = true
        )

      val html = view(submittedViewModel)

      val doc = Jsoup.parse(html.body)

      doc.text must include(
        messages("checkCorrespondenceDetails.message.requiredToSubmit")
      )
    }

    "must not display required to submit message when not submitted" in new Setup {

      val html = view(viewModel)

      val doc = Jsoup.parse(html.body)

      doc.text must not include
        messages("checkCorrespondenceDetails.message.requiredToSubmit")
    }
  }
}
