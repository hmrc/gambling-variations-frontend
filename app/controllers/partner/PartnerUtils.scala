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
import play.api.libs.json.JsArray

object PartnerUtils:
  extension (userAnswers: UserAnswers)
    def lastPartnerIndex: Int = (userAnswers.data \ "partners")
      .validate[JsArray]
      .asOpt
      .fold(0)(e => if e.value.isEmpty then 0 else e.value.size - 1)

    // TODO: this has to be fixed with the indexing ticket
    def addNewPartnerIndex(): Int = lastPartnerIndex + 1
