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

package controllers.partnerdetails

import base.SpecBase
import models.UserAnswers
import pages.partnerdetails.{PartnerDetailsChosenPartnerToRemovePage, PartnerDetailsDateOfLeavingPage, PartnerDetailsMgdRegNumberPage, PartnerDetailsTradingNamePage}
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.partnerdetails.PartnerDetailsCheckConfirmRemoveDateViewModel
import views.html.partnerdetails.PartnerDetailsCheckConfirmRemoveDateView

import java.time.LocalDate

//TODO 
class PartnerDetailsCheckConfirmRemoveDateControllerSpec extends SpecBase with PartnerDetailsHelper {

  private val partnerName: String =
    "Test Trading Name"

  private val dateToRemove: LocalDate =
    LocalDate.of(2026, 9, 19)

  private val getRoute: String =
    controllers.partnerdetails.routes.PartnerDetailsCheckConfirmRemoveDateController
      .onPageLoad()
      .url

  override val emptyUserAnswers: UserAnswers =
    UserAnswers(userAnswersId)

  private val userAnswers: UserAnswers =
    emptyUserAnswers
      .set(
        PartnerDetailsMgdRegNumberPage(newPartnersIndex1),
        userAnswersId
      )
      .success
      .value
      .set(
        PartnerDetailsChosenPartnerToRemovePage,
        businessNumber1
//        newPartnerIndex1
      )
      .success
      .value
      .set(
        PartnerDetailsTradingNamePage(newPartnersIndex1),
        partnerName
      )
      .success
      .value
      .set(
        PartnerDetailsDateOfLeavingPage(newPartnersIndex1),
        dateToRemove
      )
      .success
      .value

  private def controller(
    application: Application
  ): PartnerDetailsCheckConfirmRemoveDateController =
    application.injector
      .instanceOf[PartnerDetailsCheckConfirmRemoveDateController]

  private def view(
    application: Application
  ): PartnerDetailsCheckConfirmRemoveDateView =
    application.injector
      .instanceOf[PartnerDetailsCheckConfirmRemoveDateView]

  private def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(
      GET,
      getRoute
    )

  "PartnerCheckConfirmRemoveDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val request =
          getRequest()

        val result =
          controller(application).onPageLoad
            .apply(request)

        val viewModel =
          PartnerDetailsCheckConfirmRemoveDateViewModel.from(
            userAnswers
          )

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(application)(
            viewModel
          )(
            request,
            msgs
          ).toString
      }
    }

    "must redirect to SystemError for a GET when no existing data can be loaded" in {

      val application =
        applicationBuilder(
          userAnswers = None
        ).build()

      running(application) {

        val result =
          controller(application).onPageLoad
            .apply(getRequest())

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController
            .onPageLoad()
            .url
      }
    }
  }
}
