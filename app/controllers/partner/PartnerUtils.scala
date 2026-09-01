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

import models.UserAnswers
import play.api.libs.json.{JsArray, JsObject}

object PartnerUtils:

  extension (userAnswers: UserAnswers)

    private def maybePartners: Option[JsArray] = (userAnswers.data \ "partners")
      .validate[JsArray]
      .asOpt

    private def partnersCount: Int =
      maybePartners
        .map(_.value.size)
        .getOrElse(0)

    def partnerIndexOffset: Int =
      val count = partnersCount
      if count == 0 then 0 else count - 1

    private def allPartners: Seq[JsObject] =
      maybePartners
        .map(_.value.collect { case obj: JsObject => obj }.toSeq)
        .getOrElse(Seq.empty)

    def partnerAt(index: Int): Option[JsObject] =
      allPartners.lift(index)
