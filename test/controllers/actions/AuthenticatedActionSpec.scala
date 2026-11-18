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

package controllers.actions

import base.SpecBase
import play.api.mvc.*
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedActionSpec extends SpecBase {

  class Harness(authenticatedAction: AuthenticatedAction) {
    def onPageLoad: Action[AnyContent] = authenticatedAction { _ =>
      Results.Ok
    }
  }

  val bodyParser: BodyParsers.Default = BodyParsers.Default(Helpers.stubPlayBodyParsers)

  private def authConnectorReturning(result: Future[Option[AffinityGroup]]): AuthConnector = {
    val mockAuthConnector: AuthConnector = mock[AuthConnector]
    (mockAuthConnector
      .authorise(_: Predicate, _: Retrieval[Option[AffinityGroup]])(using _: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(result)
    mockAuthConnector
  }

  "AuthenticatedAction" - {

    Seq(AffinityGroup.Organisation, AffinityGroup.Agent).foreach { affinityGroup =>
      s"allow the request through when the user is logged in as an $affinityGroup" in {
        val authenticatedAction =
          new DefaultAuthenticatedAction(authConnectorReturning(Future.successful(Some(affinityGroup))), testFrontendAppConfig, bodyParser)

        val controller = new Harness(authenticatedAction)
        val result = controller.onPageLoad(FakeRequest("GET", "/test"))

        status(result) mustBe OK
      }
    }

    "redirect to access denied page when the user is logged in as an Individual" in {
      val authenticatedAction =
        new DefaultAuthenticatedAction(authConnectorReturning(Future.successful(Some(AffinityGroup.Individual))), testFrontendAppConfig, bodyParser)

      val controller = new Harness(authenticatedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.AccessDeniedController.onPageLoad().url)
    }

    "redirect to access denied page when the user has no affinity group" in {
      val authenticatedAction =
        new DefaultAuthenticatedAction(authConnectorReturning(Future.successful(None)), testFrontendAppConfig, bodyParser)

      val controller = new Harness(authenticatedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.AccessDeniedController.onPageLoad().url)
    }

    "redirect to login when the user has no active session" in {
      val authenticatedAction =
        new DefaultAuthenticatedAction(authConnectorReturning(Future.failed(new NoActiveSession("No session") {})), testFrontendAppConfig, bodyParser)

      val controller = new Harness(authenticatedAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/test"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value must startWith(testFrontendAppConfig.loginUrl)
    }
  }
}
