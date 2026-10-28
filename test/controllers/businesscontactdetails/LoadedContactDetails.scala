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

package controllers.businesscontactdetails

import models.{OriginalAnswers, UserAnswers}
import pages.contactdetails.BusinessContactDetailsSection
import play.api.libs.json.JsObject

import java.time.Instant

// Answers in which the business contact details have already been loaded from the backend, with the given original data
object LoadedContactDetails {

  def apply(id: String, data: JsObject): UserAnswers =
    UserAnswers(id, OriginalAnswers(data, Map(BusinessContactDetailsSection.key -> Instant.EPOCH)))
}
