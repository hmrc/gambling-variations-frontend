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

package models.licencespremises

import models.{Enumerable, UserAnswers, WithName}
import pages.{QuestionPage, licencespremises}
import pages.licencespremises.*
import viewmodels.govuk.checkbox.*

sealed trait OtherLicencesAndPermitsGB

object OtherLicencesAndPermitsGB extends Enumerable.Implicits {

  case object clubGaming                extends WithName("clubGaming") with OtherLicencesAndPermitsGB
  case object clubMachine               extends WithName("clubMachine") with OtherLicencesAndPermitsGB
  case object clubPremises              extends WithName("clubPremises") with OtherLicencesAndPermitsGB
  case object familyEntertainment       extends WithName("familyEntertainment") with OtherLicencesAndPermitsGB
  case object localAuthority            extends WithName("localAuthority") with OtherLicencesAndPermitsGB
  case object onPremises                extends WithName("onPremises") with OtherLicencesAndPermitsGB
  case object prizeGaming               extends WithName("prizeGaming") with OtherLicencesAndPermitsGB
  case object noOtherLicencesAndPermits extends WithName("none") with OtherLicencesAndPermitsGB

  val mappedValuesWithPages: Map[OtherLicencesAndPermitsGB, QuestionPage[String]] = Map(
    clubGaming          -> LicenceClubGamingPage,
    clubMachine         -> ClubLicencePage,
    clubPremises        -> LicenceClubPremisesPage,
    familyEntertainment -> LicenceFamilyEntertainmentPage,
    localAuthority      -> LicenceLocalAuthorityPage,
    onPremises          -> LicenceOnPremisesPage,
    prizeGaming         -> LicencePrizeGamingPage
  )

  val allValues: Seq[OtherLicencesAndPermitsGB] = Seq(
    clubGaming,
    clubMachine,
    clubPremises,
    familyEntertainment,
    localAuthority,
    onPremises,
    prizeGaming,
    noOtherLicencesAndPermits
  )

  val positiveValues: Seq[OtherLicencesAndPermitsGB] =
    Seq(clubGaming, clubMachine, clubPremises, familyEntertainment, localAuthority, onPremises, prizeGaming)

  def getSelectedLicencesAndPermits(ua: UserAnswers): Set[OtherLicencesAndPermitsGB] = {
    // iterates the licence/permit pages to check which has a checked "1"
    // then converts to a Set type so the form can populate previous answers
    mappedValuesWithPages.keys.filter(value => ua.get(mappedValuesWithPages(value)).contains("1")).toSet
  }

  implicit val enumerable: Enumerable[OtherLicencesAndPermitsGB] =
    Enumerable(allValues.map(v => v.toString -> v)*)
}
