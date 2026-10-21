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

package pages.returnperiods

import pages.QuestionPage
import play.api.libs.json.JsPath

import java.time.LocalDate

case object NonStandardPeriodDate1Page extends QuestionPage[LocalDate] {
  override def path: JsPath = JsPath \ "nstpEndDate1"
}

case object NonStandardPeriodDate2Page extends QuestionPage[LocalDate] {
  override def path: JsPath = JsPath \ "nstpEndDate2"
}

case object NonStandardPeriodDate3Page extends QuestionPage[LocalDate] {
  override def path: JsPath = JsPath \ "nstpEndDate3"
}

case object NonStandardPeriodDate4Page extends QuestionPage[LocalDate] {
  override def path: JsPath = JsPath \ "nstpEndDate4"
}

case object NonStandardPeriodDate5Page extends QuestionPage[LocalDate] {
  override def path: JsPath = JsPath \ "nstpEndDate5"
}

case object NonStandardPeriodDate6Page extends QuestionPage[LocalDate] {
  override def path: JsPath = JsPath \ "nstpEndDate6"
}

case object NonStandardPeriodDate7Page extends QuestionPage[LocalDate] {
  override def path: JsPath = JsPath \ "nstpEndDate7"
}

case object NonStandardPeriodDate8Page extends QuestionPage[LocalDate] {
  override def path: JsPath = JsPath \ "nstpEndDate8"
}

object NonStandardPeriodDatePages {

  private val pages: Seq[QuestionPage[LocalDate]] =
    Seq(
      NonStandardPeriodDate1Page,
      NonStandardPeriodDate2Page,
      NonStandardPeriodDate3Page,
      NonStandardPeriodDate4Page,
      NonStandardPeriodDate5Page,
      NonStandardPeriodDate6Page,
      NonStandardPeriodDate7Page,
      NonStandardPeriodDate8Page
    )

  def apply(periodNumber: Int): QuestionPage[LocalDate] =
    pages(periodNumber - 1)
}
