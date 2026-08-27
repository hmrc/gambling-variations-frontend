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

package viewmodels.checkAnswers.partner

import base.SpecBase
import models.BusinessType.Corporatebody
import pages.partnerdetails.PartnerDetailsBusinessTypePage
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class PartnerDetailsBusinessTypeSummarySpec extends SpecBase {

  lazy val app: Application = applicationBuilder().build()
  implicit val messages: Messages = this.messages(app)

  private val index: Int = 0

  "PartnerDetailsBusinessTypeSummary" - {

    "must return None when the question has not been answered" in {
      PartnerDetailsBusinessTypeSummary.row(emptyUserAnswers) mustBe None
    }

    "must return the correct row when an answer exists" in {
      val answers =
        emptyUserAnswers
          .set(PartnerDetailsBusinessTypePage(index), Corporatebody.code)
          .success
          .value

      val expectedValue = ValueViewModel(
        HtmlContent(
          HtmlFormat.escape(messages(s"partnerDetailsBusinessType.${Corporatebody.code}"))
        )
      )

      PartnerDetailsBusinessTypeSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "partnerDetailsBusinessType.checkYourAnswersLabel",
          value = expectedValue,
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url
            ).withVisuallyHiddenText(
              messages("partnerDetailsBusinessType.change.hidden")
            )
          )
        )
    }
  }
}
