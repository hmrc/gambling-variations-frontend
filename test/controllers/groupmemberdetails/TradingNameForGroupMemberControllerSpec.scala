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

package controllers.groupmemberdetails

import base.SpecBase
import models.TradingNameForGroupMember
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.groupmemberdetails.{GroupMemberDetailsSection, RemoveTradingNameForGroupMemberPage, TradingNameForGroupMemberPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import scala.concurrent.Future

class TradingNameForGroupMemberControllerSpec extends SpecBase with MockitoSugar {

  private val controller = routes.TradingNameForGroupMemberController
  private val basePath = "/change-registration-details/group-member-details"

  "TradingNameForGroupMemberController" - {

    "must store both trading name answers under the lowercase group member details section" in {
      val answers = emptyUserAnswers
        .set(TradingNameForGroupMemberPage, TradingNameForGroupMember.Yes)
        .success
        .value
        .set(RemoveTradingNameForGroupMemberPage, TradingNameForGroupMember.No)
        .success
        .value

      answers.changes(GroupMemberDetailsSection) mustEqual Json.obj(
        "groupmemberdetailssection" -> Json.obj(
          "tradingNameForGroupMember"       -> "yes",
          "removeTradingNameForGroupMember" -> "no"
        )
      )
    }

    Seq(
      (false, "trading-name-yes-no", TradingNameForGroupMemberPage, "Select yes if you want to add a trading name for the group member"),
      (true, "remove-trading-name", RemoveTradingNameForGroupMemberPage, "Select yes if you want to remove this group member’s trading name")
    ).foreach { case (remove, path, page, requiredMessage) =>
      s"at $path" - {
        def getCall = controller.onPageLoad(remove)
        def postCall = controller.onSubmit(remove)

        "must render the question with no selection on a fresh session" in {
          val application = applicationBuilder().build()

          running(application) {
            val result = route(application, FakeRequest(getCall)).value

            status(result) mustEqual OK
            getCall.url must endWith(s"$basePath/$path")
            postCall.url mustEqual getCall.url

            val document = Jsoup.parse(contentAsString(result))
            document.select("form[method=POST]").attr("action") mustEqual postCall.url
            document.select(".govuk-radios__input[checked]") mustBe empty
          }
        }

        "must preselect the saved answer for this question" in {
          val userAnswers = emptyUserAnswers.set(page, TradingNameForGroupMember.No).success.value
          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val result = route(application, FakeRequest(getCall)).value

            status(result) mustEqual OK
            val document = Jsoup.parse(contentAsString(result))
            document.select(".govuk-radios__input[checked]").attr("value") mustEqual "no"
          }
        }

        TradingNameForGroupMember.values.foreach { answer =>
          s"must save $answer and redirect to the placeholder" in {
            val otherPage = if (remove) TradingNameForGroupMemberPage else RemoveTradingNameForGroupMemberPage
            val userAnswers = emptyUserAnswers.set(otherPage, TradingNameForGroupMember.Yes).success.value
            val repository = mock[SessionRepository]
            when(repository.set(any())).thenReturn(Future.successful(true))

            val application = applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(bind[SessionRepository].toInstance(repository))
              .build()

            running(application) {
              val request = FakeRequest(postCall).withFormUrlEncodedBody("value" -> answer.toString)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual "#"
              verify(repository).set(userAnswers.set(page, answer).success.value)
            }
          }
        }

        "must render the required error for an empty submission" in {
          val repository = mock[SessionRepository]
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(repository))
            .build()

          running(application) {
            val result = route(application, FakeRequest(postCall).withFormUrlEncodedBody()).value

            status(result) mustEqual BAD_REQUEST
            val document = Jsoup.parse(contentAsString(result))
            document.select(".govuk-error-summary__list a").text() mustEqual requiredMessage
            document.select(".govuk-error-message").text() must include(requiredMessage)
            document.select("form[method=POST]").attr("action") mustEqual postCall.url
            verifyNoInteractions(repository)
          }
        }
      }
    }
  }
}
