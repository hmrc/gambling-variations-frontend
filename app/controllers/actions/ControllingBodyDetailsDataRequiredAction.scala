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

package controllers.actions

import connectors.GamblingConnector
import controllers.routes
import models.{Address, ContactNumber, CorrespondenceDetails, SoleProprietorName, UserAnswers}
import models.controllingbody.ControlBodyDetails
import models.requests.{DataRequest, OptionalDataRequest}
import pages.controllingbody.*
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

class ControllingBodyDetailsDataRequiredActionImpl @Inject() (
  val sessionRepository: SessionRepository,
  val gamblingConnector: GamblingConnector
)(implicit val executionContext: ExecutionContext)
    extends ControllingBodyDetailsDataRequiredAction
    with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    val answers = request.userAnswers.getOrElse(UserAnswers(request.mgdRegNum))
    // Only the full-section marker proves all details were fetched. The old
    // name-only "loaded" flag must not prevent loading addresses and identifiers.
    answers.get(ControllingBodySectionPage) match {
      case Some(_) => Future.successful(Right(DataRequest(request.request, request.mgdRegNum, answers)))
      case None =>
        given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        (for {
          details        <- gamblingConnector.getControlBodyDetails(request.mgdRegNum)
          updatedAnswers <- Future.fromTry(setControllingBodyDetails(details, answers))
          saved          <- sessionRepository.set(updatedAnswers)
        } yield saved match {
          case true  => Right(DataRequest(request.request, request.mgdRegNum, updatedAnswers))
          case false => Left(Redirect(routes.SystemErrorController.onPageLoad()))
        }).recover { case NonFatal(e) =>
          logger.warn("Unable to load controlling body details", e)
          Left(Redirect(routes.SystemErrorController.onPageLoad()))
        }
    }
  }

  private def setControllingBodyDetails(details: ControlBodyDetails, answers: UserAnswers): Try[UserAnswers] = {

    val address = details.address1.map { address1 =>
      Address(
        address1 = address1,
        address2 = details.address2,
        address3 = details.address3,
        address4 = details.address4,
        postcode = details.postcode,
        country  = details.country
      )
    }

    val contactNumberDetails: Option[ContactNumber] =
      (details.phoneNumber, details.mobilePhoneNumber) match {
        case (None, None) => None
        case (phoneNumber, mobilePhoneNumber) =>
          Some(ContactNumber(phoneNumber, mobilePhoneNumber))
      }

    val correspondenceDetails = CorrespondenceDetails(
      mgdRegNumber          = details.mgdRegNumber,
      nameLine1             = None,
      nameLine2             = None,
      correspondenceAddress = address,
      additionalInformation = details.adi,
      iomOrCiFlag           = details.isIomOrCiFlag,
      contactNumber         = contactNumberDetails,
      faxNumber             = details.faxNumber,
      emailAddr             = details.emailAddr
    )

    for {
      updatedAnswers <- answers.set(ControllingBodySectionPage, details.mgdRegNumber)
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyBusinessPartnerNumberPage, details.businessPartnerNumber)
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyDateOfJoiningPage, details.dateOfJoining)
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyDateOfLeavingPage, details.dateOfLeaving)
      updatedAnswers <- updatedAnswers.set(ControllingBodyCorrespondenceSectionPage, correspondenceDetails)
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyDateOfIncorporationPage, details.dateOfIncorporation)
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyCountryOfIncorporationPage, details.countryOfIncorporation)
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyForeignCorporateReferencePage, details.foreignCorporateRef)
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyDateOfBirthPage, details.dateOfBirth)
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyNinoPage, details.nino)
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyUtrPage, details.utr.map(_.toString))
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyVrnPage, details.vrn.map(_.toString))
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyCrnPage, details.crn)
      updatedAnswers <-
        updatedAnswers.setIfDefined(ControllingBodyBusinessNamePage, answers.get(ControllingBodyBusinessNamePage).orElse(details.businessName))
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyTradingNamePage, details.tradingName)
      updatedAnswers <-
        updatedAnswers.setIfDefined(ControllingBodyBusinessTypePage,
                                    answers.get(ControllingBodyBusinessTypePage).orElse(details.typeOfControllingBody)
                                   )
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyIsRepMemSameAsCbPage, details.isRepMemSameAsCb)
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodyIsUkIncorporatedPage, details.isUkIncorporated)
      updatedAnswers <- updatedAnswers.setIfDefined(ControllingBodySoleProprietorPage,
                                                    answers.get(ControllingBodySoleProprietorPage).orElse(buildSoleProprietorName(details))
                                                   )
    } yield updatedAnswers
  }

  private def buildSoleProprietorName(details: ControlBodyDetails): Option[SoleProprietorName] =
    Option.when(Seq(details.solePropTitle, details.solePropFirstName, details.solePropMiddleName, details.solePropLastName).exists(_.isDefined))(
      SoleProprietorName(
        details.solePropTitle.getOrElse(""),
        details.solePropFirstName.getOrElse(""),
        details.solePropMiddleName,
        details.solePropLastName.getOrElse("")
      )
    )

}

trait ControllingBodyDetailsDataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
