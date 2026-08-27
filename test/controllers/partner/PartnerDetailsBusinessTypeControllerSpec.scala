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
import controllers.partner.routes.PartnerDetailsBusinessTypeController
import controllers.routes
import forms.partner.PartnerDetailsBusinessTypeFormProvider
import models.{NormalMode, PartnerDetailsBusinessType, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.PartnerDetailsBusinessTypePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerDetailsBusinessTypeView

import scala.concurrent.Future

class PartnerDetailsBusinessTypeControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  def onwardRoute = Call("GET", "/foo")

  lazy val partnerDetailsBusinessTypeRoute: String = {
    PartnerDetailsBusinessTypeController.onPageLoad().url
  }

  val formProvider: PartnerDetailsBusinessTypeFormProvider = new PartnerDetailsBusinessTypeFormProvider()

  "PartnerDetailsBusinessType Controller" - {}
}
