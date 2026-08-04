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
import config.FrontendAppConfig
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.play.language.LanguageUtils

class LanguageSwitchControllerSpec extends SpecBase with MockitoSugar {

  "LanguageSwitchController" - {

    "must return the configured language map" in {

      val mockAppConfig = mock[FrontendAppConfig]
      val mockLanguageUtils = mock[LanguageUtils]
      val mockControllerComponents = mock[ControllerComponents]

      val expectedLanguageMap = Map(
        "en" -> Lang("en"),
        "cy" -> Lang("cy")
      )

      when(mockAppConfig.languageMap).thenReturn(expectedLanguageMap)

      val controller =
        new LanguageSwitchController(
          mockAppConfig,
          mockLanguageUtils,
          mockControllerComponents
        )

      controller.languageMap mustEqual expectedLanguageMap
    }

    "must return the index controller URL as fallback URL" in {

      val mockAppConfig = mock[FrontendAppConfig]
      val mockLanguageUtils = mock[LanguageUtils]
      val mockControllerComponents = mock[ControllerComponents]

      val controller =
        new LanguageSwitchController(
          mockAppConfig,
          mockLanguageUtils,
          mockControllerComponents
        )

      controller.fallbackURL mustEqual routes.IndexController.onPageLoad().url
    }
  }
}
