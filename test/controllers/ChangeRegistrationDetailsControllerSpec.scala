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

package controllers

import base.SpecBase
import connectors.GamblingConnector
import models.BusinessType.Unincorporatedbody
import models.controllingbody.ControlBodyDetails
import models.{BusinessDetails, BusinessType, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.*
import pages.businessname.BusinessNameChangesPage
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

class ChangeRegistrationDetailsControllerSpec extends SpecBase with MockitoSugar {

  private def request = FakeRequest(GET, routes.ChangeRegistrationDetailsController.onPageLoad().url)

  private val partnershipAnswers: UserAnswers = emptyUserAnswers.set(BusinessTypePage, BusinessType.Partnership).success.value

  private def sectionNames(content: String): Seq[String] =
    Jsoup.parse(content).select("table.govuk-table tbody tr td:first-child a").asScala.map(_.text).toSeq

  private def statuses(content: String): Seq[String] =
    Jsoup.parse(content).select("table.govuk-table tbody tr td:last-child").asScala.map(_.text).toSeq

  private def controlBodyDetails(
    mgdRegNumber: String,
    isRepMemSameAsCb: Option[String]
  ): ControlBodyDetails =
    ControlBodyDetails(
      mgdRegNumber           = mgdRegNumber,
      businessPartnerNumber  = None,
      dateOfJoining          = None,
      dateOfLeaving          = None,
      solePropTitle          = None,
      solePropFirstName      = None,
      solePropMiddleName     = None,
      solePropLastName       = None,
      businessName           = None,
      tradingName            = None,
      dateOfBirth            = None,
      nino                   = None,
      utr                    = None,
      vrn                    = None,
      crn                    = None,
      dateOfIncorporation    = None,
      countryOfIncorporation = None,
      foreignCorporateRef    = None,
      address1               = None,
      address2               = None,
      address3               = None,
      address4               = None,
      postcode               = None,
      country                = None,
      adi                    = None,
      isIomOrCiFlag          = None,
      phoneNumber            = None,
      mobilePhoneNumber      = None,
      faxNumber              = None,
      emailAddr              = None,
      typeOfControllingBody  = None,
      isRepMemSameAsCb       = isRepMemSameAsCb,
      isUkIncorporated       = None
    )

  private def applicationWith(
    userAnswers: UserAnswers,
    details: ControlBodyDetails
  ): Application = {
    val gamblingConnector = mock[GamblingConnector]

    when(
      gamblingConnector.getControlBodyDetails(any[String])(any[HeaderCarrier])
    ).thenReturn(Future.successful(details))

    applicationBuilder(userAnswers = Some(userAnswers))
      .overrides(bind[GamblingConnector].toInstance(gamblingConnector))
      .build()
  }

  private def controllingBodyLink(content: String): String =
    Jsoup.parse(content).select("table.govuk-table tbody tr td:first-child a").asScala.find(_.text == "Controlling body details").value.attr("href")

  "must return OK and render the sections for a partnership that is not a group member" in {

    val userAnswers =
      partnershipAnswers.set(GroupMemberPage, false).success.value

    val application =
      applicationWith(
        userAnswers,
        controlBodyDetails("", None)
      )

    running(application) {
      val result = route(application, request).value

      status(result) mustEqual OK

      sectionNames(contentAsString(result)) mustEqual Seq(
        "Business name",
        "Business address",
        "Business contact details",
        "Correspondence details",
        "Partner details",
        "Trading details",
        "Licences and premises",
        "Return periods"
      )
    }
  }

  "must return OK and render the sections for a group member" in {

    val userAnswers =
      partnershipAnswers.set(GroupMemberPage, true).success.value

    val application =
      applicationWith(
        userAnswers,
        controlBodyDetails("XGM00000001761", Some("1"))
      )

    running(application) {
      val result = route(application, request).value

      status(result) mustEqual OK

      sectionNames(contentAsString(result)) mustEqual Seq(
        "Controlling body details",
        "Group member details",
        "Correspondence details",
        "Partner details",
        "Trading details",
        "Return periods"
      )
    }
  }

  "must show every section as unchanged and hide the submit button when nothing has changed" in {

    val userAnswers =
      partnershipAnswers.set(GroupMemberPage, false).success.value

    val application =
      applicationWith(
        userAnswers,
        controlBodyDetails("", None)
      )

    running(application) {
      val result = route(application, request).value
      val content = contentAsString(result)

      status(result) mustEqual OK

      statuses(content).distinct mustEqual Seq("No details changed")

      content must include(
        "You need to change at least one detail before you can submit your changes to HMRC."
      )
      Jsoup.parse(content).select(".govuk-button").size mustEqual 0
    }
  }

  "must show the submit button when a section has changes" in {

    val userAnswers =
      partnershipAnswers
        .set(GroupMemberPage, false)
        .success
        .value
        .set(BusinessNameChangesPage, true)
        .success
        .value

    val application =
      applicationWith(
        userAnswers,
        controlBodyDetails("", None)
      )

    running(application) {
      val result = route(application, request).value
      val content = contentAsString(result)

      status(result) mustEqual OK

      statuses(content).head mustEqual "Changes ready to submit"

      val button = Jsoup.parse(content).select(".govuk-button")

      button.size mustEqual 1
      button.text mustEqual "Submit changes"
      button.attr("href") mustEqual routes.DeclarationController.onPageLoad().url

      content must not include
        "You need to change at least one detail before you can submit your changes to HMRC."
    }
  }

  "must redirect to SystemErrorController when GroupMemberPage is missing" in {

    val application =
      applicationBuilder(userAnswers = Some(partnershipAnswers)).build()

    running(application) {
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        routes.SystemErrorController.onPageLoad().url
    }
  }

  "must link controlling body details to the not provided page when the API returns no details" in {

    val userAnswers =
      partnershipAnswers.set(GroupMemberPage, true).success.value

    val application =
      applicationWith(
        userAnswers,
        controlBodyDetails("", None)
      )

    running(application) {
      val result = route(application, request).value
      val content = contentAsString(result)

      status(result) mustEqual OK

      controllingBodyLink(content) mustEqual
        controllers.controllingbody.routes.ControllingBodyNotProvidedYesNoController
          .onPageLoad()
          .url
    }
  }

  "must link controlling body details to the same representative page when the API flag is 1" in {

    val userAnswers =
      partnershipAnswers.set(GroupMemberPage, true).success.value

    val application =
      applicationWith(
        userAnswers,
        controlBodyDetails("XGM00000001761", Some("1"))
      )

    running(application) {
      val result = route(application, request).value
      val content = contentAsString(result)

      status(result) mustEqual OK

      controllingBodyLink(content) mustEqual
        controllers.controllingbody.routes.ControllingBodySameAsRepYesNoController
          .onPageLoad()
          .url
    }
  }

  "must link controlling body details to the change screener when the API flag is 0" in {

    val userAnswers =
      partnershipAnswers.set(GroupMemberPage, true).success.value

    val application =
      applicationWith(
        userAnswers,
        controlBodyDetails("XGM00000001761", Some("0"))
      )

    running(application) {
      val result = route(application, request).value
      val content = contentAsString(result)

      status(result) mustEqual OK

      controllingBodyLink(content) mustEqual
        controllers.controllingbody.routes.ControllingBodyChangeScreenerController
          .onPageLoad()
          .url
    }
  }

  "must link controlling body details to page not found when the API flag is invalid" in {

    val userAnswers =
      partnershipAnswers.set(GroupMemberPage, true).success.value

    val application =
      applicationWith(
        userAnswers,
        controlBodyDetails("XGM00000001761", Some("invalid"))
      )

    running(application) {
      val result = route(application, request).value
      val content = contentAsString(result)

      status(result) mustEqual OK

      controllingBodyLink(content) mustEqual
        routes.PageNotFoundController.onPageLoad().url
    }
  }

  "must redirect to SystemErrorController when retrieving controlling body details fails" in {

    val userAnswers =
      partnershipAnswers.set(GroupMemberPage, true).success.value

    val gamblingConnector = mock[GamblingConnector]

    when(
      gamblingConnector.getControlBodyDetails(
        any[String]
      )(any[HeaderCarrier])
    ).thenReturn(Future.failed(new RuntimeException("API failure")))

    val application =
      applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[GamblingConnector].toInstance(gamblingConnector))
        .build()

    running(application) {
      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        routes.SystemErrorController.onPageLoad().url
    }
  }
}
