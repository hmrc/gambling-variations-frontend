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

import models.UserAnswers
import pages.QuestionPage
import pages.licencespremises.*

import scala.util.Try

object LicencesPremisesAnswers {

  extension (answers: UserAnswers) {

    // The backend holds booleans as "1" or "0", an oracle implementation detail that has been propagated through 3 layers of microservices.
    def backendFlagOption(page: QuestionPage[String]): Option[Boolean] =
      answers.get(page).map {
        case "1"   => true
        case "0"   => false
        case value => throw new IllegalArgumentException(s"Unexpected value '$value' for $page, expected 1 or 0")
      }

    // A missing flag is treated as indicating false.
    def backendFlag(page: QuestionPage[String]): Boolean =
      backendFlagOption(page).getOrElse(false)

    // Answers given in this session are written back in the backend format, so the backend key always holds the current answer
    def setBackendFlag(page: QuestionPage[String], value: Boolean): Try[UserAnswers] =
      answers.set(page, if (value) "1" else "0")

    def pubTenantAnswer: Boolean = backendFlag(LicenceHeldByLandlordPage)

    def premisesNotCoveredAnswer: Boolean = backendFlag(LicencePremisesNotCoveredPage)

    def licenceNumberAnswer: Option[String] =
      answers.get(LicenceNumberPage).map(_.trim).filter(_.nonEmpty)

    def licencesAndPermitsGB: Seq[OtherLicencesAndPermitsGB] =
      OtherLicencesAndPermitsGB.positiveValues.filter(value => backendFlag(OtherLicencesAndPermitsGB.mappedValuesWithPages(value)))

    def licencesAndPermitsNI: Seq[OtherLicencesAndPermitsNI] =
      OtherLicencesAndPermitsNI.positiveValues.filter(value => backendFlag(OtherLicencesAndPermitsNI.mappedValuesWithPages(value)))

    // The premises not covered question is only in scope when some kind of licence or permit has been provided
    def hasLicencesOrPermits: Boolean =
      licenceNumberAnswer.isDefined || pubTenantAnswer || licencesAndPermitsGB.nonEmpty || licencesAndPermitsNI.nonEmpty

    // The premises are counted rather than using totalRows, so that the count agrees with the premises addresses list, also after removals
    def premisesCount: Int =
      answers.get(PremisesDetailsPage).map(_.premises.size).getOrElse(0)

    // The check page and the provide premises addresses question show the same method, derived when it has not been answered
    def provideAddressesAnswer: LicencesAndPremisesRadioOptions =
      answers.get(LicencesPremisesPage).getOrElse(LicencesAndPremisesRadioOptions.derivedFrom(premisesCount))

    // The section is flagged as submitted once a change screen has been continued from, and as changed once any answer differs
    def withLicencesPremisesFlags(isChanged: Boolean): Try[UserAnswers] = {
      val isAlreadyFlagged = answers.get(LicencesPremisesDetailsChangesPage).contains(true)

      for {
        withSubmitted <- answers.set(LicencesPremisesDetailsSubmittedPage, true)
        withChanges   <- withSubmitted.set(LicencesPremisesDetailsChangesPage, isChanged || isAlreadyFlagged)
      } yield withChanges
    }
  }
}
