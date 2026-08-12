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

package controllers.partner

import base.SpecBase
import controllers.partner.routes.PartnerRemoveFaxNumberYesNoController
import forms.partner.PartnerRemoveFaxNumberYesNoFormProvider
import models.{CorrespondenceDetails, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsCorrespondenceDetailsSectionPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.PartnerRemoveFaxNumberYesNoView

import scala.concurrent.Future

class PartnerRemoveFaxNumberYesNoControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val formProvider = new PartnerRemoveFaxNumberYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val partnerRemoveFaxNumberYesNoRoute: String = PartnerRemoveFaxNumberYesNoController.onPageLoad().url
  private val testFaxNumber = "02071234568"
  private val index = 0
  private val mgdRegNumber = "XGM00000001761"
  private val correspondenceDetailsWithFax = CorrespondenceDetails(
    mgdRegNumber          = mgdRegNumber,
    nameLine1             = None,
    nameLine2             = None,
    correspondenceAddress = None,
    additionalInformation = None,
    iomOrCiFlag           = None,
    contactNumber         = None,
    faxNumber             = Some(testFaxNumber),
    emailAddr             = None
  )

  "PartnerRemoveFaxNumberYesNo Controller" - {

    "must redirect to the next page when valid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerRemoveFaxNumberYesNoRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val cleanedData = Json.obj(
        "partners" -> Json.arr(
          Json.obj(
            "partnerDetailsMgdRegNumber" -> "XGM00000001761",
            "partnerDetailsBusinessName" -> "Partner1",
            "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
              "mgdRegNumber" -> "XGM00000001761",
              "correspondenceAddress" -> Json.obj(
                "address1" -> "Flat 1",
                "address2" -> "10 Market Road",
                "address3" -> "Felling",
                "address4" -> "Gateshead",
                "postcode" -> "NE8 1ZZ",
                "country"  -> "UK"
              ),
              "contactNumber" -> Json.obj(
                "phoneNumber"       -> "0798765",
                "mobilePhoneNumber" -> "7093434765"
              ),
              "faxNumber" -> testFaxNumber,
              "emailAddr" -> "a@b.com"
            )
          )
        ),
        "partnerRemoveFaxNumberYesNo" -> true
      )

      val userAnswers = UserAnswers("XGM00000001761", cleanedData)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerRemoveFaxNumberYesNoRoute)

        val view = application.injector.instanceOf[PartnerRemoveFaxNumberYesNoView]

        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, testFaxNumber)(request, messages(application)).toString
      }
    }

  }
}
