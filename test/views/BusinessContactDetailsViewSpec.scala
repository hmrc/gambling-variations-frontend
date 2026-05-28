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
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import models.{BusinessContactDetails, BusinessType, NormalMode}
import views.html.BusinessContactDetailsView
import views.html.helper.form

import java.time.LocalDate

class BusinessContactDetailsViewSpec extends SpecBase {

  trait Setup {
    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[BusinessContactDetailsView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)
  }

  "BusinessContactDetailsView" - {
    "must show expected values when data is populated" in new Setup {
      val populatedData: BusinessContactDetails =
        BusinessContactDetails(
          mgdRegNumber = "Mr",
          phoneNumber = Some("John"),
          mobilePhoneNumber = Some("John"),
          faxNumber = Some("John"),
          emailAddr = Some("John"),
          systemDate = Some(LocalDate.of(1991,1,1),
      )
      val html = view(populatedData)
      val doc = Jsoup.parse(html.body)
      
    }

  }
}
