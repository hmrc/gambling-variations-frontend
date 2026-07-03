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

package viewmodels.checkAnswers.tradingdetails

import base.SpecBase
import models.BusinessTradeClass
import pages.*
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

class CheckTradingDetailsViewModelSpec extends SpecBase {

  private val app: Application = applicationBuilder().build()

  implicit val msgs: Messages = messages(app)

  "CheckTradingDetailsViewModel.from" - {

    "must not populate trade class, previous MGD or associated MGD lists for group members" in {

      val answers =
        emptyUserAnswers
          .set(GroupMemberPage, true)
          .success
          .value

      val result =
        CheckTradingDetailsViewModel.from(answers, isGroupMember = true)

      result.list.rows mustBe Nil
      result.previousMgd.rows mustBe Nil
      result.associatedMgd.rows mustBe Nil
    }

    "must populate trade class & seasonal business rows for non-group members" in {

      val answers =
        emptyUserAnswers
          .set(GroupMemberPage, false)
          .success
          .value
          .set(BusinessTradeClassPage, BusinessTradeClass.Casino)
          .success
          .value

      val result =
        CheckTradingDetailsViewModel.from(answers, isGroupMember = false)

      result.list.rows.nonEmpty mustBe true
      result.list.rows.head.key.content mustEqual Text("Trade class")
      result.list.rows(1).key.content mustEqual Text("Seasonal business")
    }

    "must include the other trade class row when trade class is Other" in {

      val answers =
        emptyUserAnswers
          .set(GroupMemberPage, false)
          .success
          .value
          .set(BusinessTradeClassPage, BusinessTradeClass.Other)
          .success
          .value
          .set(OtherTradeClassPage, "Mobile gaming arcade operator")
          .success
          .value

      val result =
        CheckTradingDetailsViewModel.from(answers, isGroupMember = false)

      result.list.rows.size mustBe 2
    }

    "must not include the other trade class row when trade class is not Other" in {

      val answers =
        emptyUserAnswers
          .set(GroupMemberPage, false)
          .success
          .value
          .set(BusinessTradeClassPage, BusinessTradeClass.Casino)
          .success
          .value

      val result =
        CheckTradingDetailsViewModel.from(answers, isGroupMember = false)

      result.list.rows.size mustBe 2
      result.list.rows.head.key.content mustEqual Text("Trade class")
      result.list.rows(1).key.content mustEqual Text("Seasonal business")
    }
  }
}