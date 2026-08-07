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
import models.requests.{DataRequest, OptionalDataRequest}
import models.{Address, ContactNumber, CorrespondenceDetails, PartnerDetails, PartnersDetails, SoleProprietorName, UserAnswers}
import pages.*
import pages.partnerdetails.*
import play.api.Logging
import play.api.libs.json.Writes
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

class PartnerDetailsDataRequiredActionImpl @Inject() (
  val sessionRepository: SessionRepository,
  val gamblingConnector: GamblingConnector
)(implicit val executionContext: ExecutionContext)
    extends PartnerDetailsDataRequiredAction
    with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    request.userAnswers match {
      case None =>
        logger.info(s"User Answers not found. Populating User Answers to id ${request.mgdRegNum}")

        given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        val answers = UserAnswers(request.mgdRegNum)
        saveUserAnswersToSessionAndRedirect(answers, request)

      case Some(userAnswers) =>
        logger.info(s"User Answers found with id ${userAnswers.id}")

        // TODO this is a check to see if a particular element is present
//        userAnswers.get(PartnerDetailsBusinessEmailPage) map { _ =>
        Future.successful(Right(DataRequest(request.request, request.mgdRegNum, userAnswers)))
//        } getOrElse {
//          given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
//          saveUserAnswersToSessionAndRedirect(userAnswers, request)
//        }
    }
  }

  private def saveUserAnswersToSessionAndRedirect[A](answers: UserAnswers, request: OptionalDataRequest[A])(using HeaderCarrier) = {
    gamblingConnector.getPartnerDetails(answers.id) flatMap { mgdContactDetails =>
      setPartnerDetails(mgdContactDetails, answers) map { updatedAnswers =>
        logger.info("User Answers not found. Saving User Answers")
        sessionRepository.set(updatedAnswers) map {
          case true =>
            logger.info("User Answers saved.")
            Right(DataRequest(request.request, request.mgdRegNum, updatedAnswers))
          case false =>
            logger.info("User Answers failed.")
            Left(Redirect(routes.SystemErrorController.onPageLoad()))
        }
      } getOrElse Future.successful(Left(Redirect(routes.SystemErrorController.onPageLoad())))

    } recover { case NonFatal(e) =>
      logger.warn(s"Unable to populate User Answers for id ${request.mgdRegNum}", e)
      Left(Redirect(routes.SystemErrorController.onPageLoad()))
    }
  }

  private def setIfDefined[A](userAnswers: UserAnswers, optional: Option[A], page: QuestionPage[A])(implicit wrt: Writes[A]): Try[UserAnswers] =
    optional.fold(Try(userAnswers)) { value =>
      userAnswers.set(page, value)
    }

  private def buildPartnerDetails(partnerDetails: PartnerDetails, index: Int, userAnswers: Try[UserAnswers]) = {

    val address = partnerDetails.address1 match {
      case Some(address1) =>
        Some(
          Address(
            address1 = address1,
            address2 = partnerDetails.address2,
            address3 = partnerDetails.address3,
            address4 = partnerDetails.address4,
            postcode = partnerDetails.postcode,
            country  = partnerDetails.country
          )
        )
      case _ => None
    }

    val soleProprietor =
      (partnerDetails.solePropTitle, partnerDetails.solePropFirstName, partnerDetails.solePropMiddleName, partnerDetails.solePropLastName) match {
        case (Some(title), Some(firstName), middleName, Some(lastName)) =>
          Some(
            SoleProprietorName(
              title      = title,
              firstName  = firstName,
              middleName = middleName,
              lastName   = lastName
            )
          )
        case _ => None
      }

    val contactNumber = (partnerDetails.phoneNumber, partnerDetails.mobilePhoneNumber) match {
      case (None, None) => None
      case (a, b)       => Some(ContactNumber(a, b))
    }

    val correspondenceDetails = CorrespondenceDetails(
      mgdRegNumber          = partnerDetails.mgdRegNumber,
      nameLine1             = None, // TODO - what would it be?
      nameLine2             = None, // TODO - what would it be?
      correspondenceAddress = address,
      additionalInformation = partnerDetails.adi, // TODO is adi = additional information
      iomOrCiFlag           = partnerDetails.iomOrCiFlag,
      contactNumber         = contactNumber,
      faxNumber             = partnerDetails.faxNumber,
      emailAddr             = partnerDetails.emailAddress
    )

    for {
      updatedAnswers <- userAnswers
      updatedAnswers <- updatedAnswers.set(PartnerDetailsPage(index), partnerDetails.mgdRegNumber)
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.dateOfJoining, PartnerDetailsDateOfJoiningPage(index))
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.dateOfLeaving, PartnerDetailsDateOfLeavingPage(index))

      updatedAnswers <- setIfDefined(updatedAnswers, soleProprietor, PartnerDetailsSoleProprietorPage(index))

      updatedAnswers <- updatedAnswers.set(PartnerDetailsCorrespondenceDetailsSectionPage(index), correspondenceDetails)

      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.dateOfIncorporation, PartnerDetailsDateOfIncorporation(index))
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.countryOfIncorporation, PartnerDetailsCountryOfIncorporation(index))
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.foreignCorporateRef, PartnerDetailsForeignCorporateRefPage(index))

      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.businessName, PartnerDetailsBusinessNamePage(index))
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.tradingName, PartnerDetailsTradingNamePage(index))
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.dateOfBirth, PartnerDetailsDateOfBirthPage(index))
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.nino, PartnerDetailsNinoPage(index))
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.utr, PartnerDetailsUtrPage(index))
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.vrn, PartnerDetailsVrnPage(index))
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.crn, PartnerDetailsCrnPage(index))

    } yield updatedAnswers
  }

  private def setPartnerDetails(partnersDetails: PartnersDetails, answers: UserAnswers): Try[UserAnswers] = partnersDetails.partners.zipWithIndex
    .foldLeft(Try(answers)) { case (userAnswers, (partnerDetails, index)) =>
      buildPartnerDetails(partnerDetails, index, userAnswers)
    }

}

trait PartnerDetailsDataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
