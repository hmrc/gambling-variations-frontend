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

sealed trait OtherLicencesAndPermitsNI

object OtherLicencesAndPermitsNI extends Enumerable.Implicits {

  case object amusement                 extends WithName("amusement") with OtherLicencesAndPermitsNI
  case object bingo                     extends WithName("bingo") with OtherLicencesAndPermitsNI
  case object bookmaking                extends WithName("bookmaking") with OtherLicencesAndPermitsNI
  case object serveAlcohol              extends WithName("serveAlcohol") with OtherLicencesAndPermitsNI
  case object regCert                   extends WithName("regCert") with OtherLicencesAndPermitsNI
  case object noOtherLicencesAndPermits extends WithName("noOtherLicencesAndPremisesNISelected") with OtherLicencesAndPermitsNI

  val mappedValuesWithPages: Map[OtherLicencesAndPermitsNI, QuestionPage[String]] = Map(
    amusement                 -> LicenceAmusementPage,
    bingo                     -> LicenceBingoPage,
    bookmaking                -> LicenceBookmakingPage,
    serveAlcohol              -> LicenceServeAlcoholPage,
    regCert                   -> LicenceRegCertPage,
    noOtherLicencesAndPermits -> NoOtherLicencesAndPermitsNIPage
  )

  val values: Seq[OtherLicencesAndPermitsNI] = mappedValuesWithPages.keys.toSeq

  val positiveValues: Seq[OtherLicencesAndPermitsNI] =
    Seq(amusement, bingo, bookmaking, serveAlcohol, regCert)

  def getSelectedLicencesAndPermits(ua: UserAnswers): Set[OtherLicencesAndPermitsNI] = {
    // iterates the licence/permit pages to check which has a checked "1"
    // then converts to a Set type so the form can populate previous answers
    // defaults to noOtherLicencesAndPermits if all are 0
    val setOfLicencesAndPremises = mappedValuesWithPages.keys
      .filter(value =>
        ua.get(mappedValuesWithPages(value))
          .contains("1")
      )
      .toSet
    if (setOfLicencesAndPremises.isEmpty) Set(noOtherLicencesAndPermits) else setOfLicencesAndPremises
  }

  implicit val enumerable: Enumerable[OtherLicencesAndPermitsNI] =
    Enumerable(values.map(v => v.toString -> v)*)
}
