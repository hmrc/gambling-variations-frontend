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

package pages.controllingbody

import base.SpecBase
import models.{BusinessType, SoleProprietorName}
import pages.businessname.{BusinessNamePage, SoleProprietorPage}

class ControllingBodyNamePagesSpec extends SpecBase {

  "Controlling body name pages" - {
    "keep the controlling body type and names separate from the registered business" in {
      val registeredName = SoleProprietorName("Mr", "Tom", None, "Smith")
      val controllingName = SoleProprietorName("Ms", "Jane", Some("Mary"), "Jones")
      val answers = (for {
        answers <- emptyUserAnswers.set(BusinessNamePage, "Registered Business")
        answers <- answers.set(SoleProprietorPage, registeredName)
        answers <- answers.set(ControllingBodyBusinessTypePage, BusinessType.Corporatebody)
        answers <- answers.set(ControllingBodyBusinessNamePage, "Controlling Body")
        answers <- answers.set(ControllingBodySoleProprietorPage, controllingName)
      } yield answers).success.value

      answers.get(BusinessNamePage).value mustEqual "Registered Business"
      answers.get(SoleProprietorPage).value mustEqual registeredName
      answers.get(ControllingBodyBusinessTypePage).value mustEqual BusinessType.Corporatebody
      answers.get(ControllingBodyBusinessNamePage).value mustEqual "Controlling Body"
      answers.get(ControllingBodySoleProprietorPage).value mustEqual controllingName
    }
  }
}
