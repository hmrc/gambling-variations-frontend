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
import models.{Address, BusinessType, ContactNumber, CorrespondenceDetails, PartnerDetails, PartnersDetails, SoleProprietorName, UserAnswers}
import pages.*
import pages.partnerdetails.*
import play.api.Logging
import play.api.libs.json.Writes
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.LocalDate
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

        userAnswers.get(PartnerDetailsPage(0)) map { _ =>
          logger.info(s"MgdRegNum found for PartnerDetails with id ${userAnswers.id}")

          Future.successful(Right(DataRequest(request.request, request.mgdRegNum, userAnswers)))
        } getOrElse {
          logger.info(s"User Answers found with id ${userAnswers.id}")

          given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
          saveUserAnswersToSessionAndRedirect(userAnswers, request)
        }
    }
  }

  private def saveUserAnswersToSessionAndRedirect[A](answers: UserAnswers, request: OptionalDataRequest[A])(using HeaderCarrier) = {
    gamblingConnector.getPartnersDetails(answers.id) flatMap { partnerDetails =>
      setPartnerDetails(partnerDetails, answers) map { updatedAnswers =>
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

  private def setPartnerDetails(
    partnersDetails: PartnersDetails,
    answers: UserAnswers
  ): Try[UserAnswers] =
    partnersDetails.partners
      .filterNot(_.dateOfLeaving.exists(_.isBefore(LocalDate.now())))
      .zipWithIndex
      .foldLeft(Try(answers)) { case (userAnswers, (partnerDetails, index)) =>
        buildPartnerDetails(partnerDetails, index, userAnswers)
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

    val contactNumber = (partnerDetails.phoneNumber, partnerDetails.mobilePhoneNumber) match {
      case (None, None) => None
      case (a, b)       => Some(ContactNumber(a, b))
    }

    val correspondenceDetails = CorrespondenceDetails(
      mgdRegNumber          = partnerDetails.mgdRegNumber,
      nameLine1             = None,
      nameLine2             = None,
      correspondenceAddress = address,
      additionalInformation = partnerDetails.adi,
      iomOrCiFlag           = partnerDetails.iomOrCiFlag,
      contactNumber         = contactNumber,
      faxNumber             = partnerDetails.faxNumber,
      emailAddr             = partnerDetails.emailAddr
    )

    for {
      updatedAnswers <- userAnswers
      updatedAnswers <- setIfDefinedBusinessDetails(partnerDetails, updatedAnswers, index)
      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsBusinessPartnerNumberPage(index), partnerDetails.businessPartnerNumber)
      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsDateOfJoiningPage(index), partnerDetails.dateOfJoining)
      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsDateOfLeavingPage(index), partnerDetails.dateOfLeaving)

      updatedAnswers <- updatedAnswers.set(PartnerDetailsCorrespondenceDetailsSectionPage(index), correspondenceDetails)

      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsDateOfIncorporation(index), partnerDetails.dateOfIncorporation)
      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsCountryOfIncorporation(index), partnerDetails.countryOfIncorporation)
      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsForeignCorporateRefPage(index), partnerDetails.foreignCorporateRef)

      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsDateOfBirthPage(index), partnerDetails.dateOfBirth)
      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsNinoPage(index), partnerDetails.nino)
      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsUtrPage(index), partnerDetails.utr)
      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsVrnPage(index), partnerDetails.vrn)
      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsCrnPage(index), partnerDetails.crn)

      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsIsFutureLeaveDatePage(index), partnerDetails.isFutureLeaveDate)
      updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsIsFutureJoinDatePage(index), partnerDetails.isFutureJoinDate)
    } yield updatedAnswers
  }

  private def setIfDefinedBusinessDetails(partnerDetails: PartnerDetails, answers: UserAnswers, index: Int): Try[UserAnswers] = for {
    updatedAnswers <- answers.set(PartnerDetailsPage(index), partnerDetails.mgdRegNumber)
    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsTradingNamePage(index), partnerDetails.tradingName)
    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsBusinessTypePage(index), partnerDetails.businessType)

    updatedAnswers <- partnerDetails.businessType
                        .flatMap(BusinessType.fromCode)
                        // If BusinessType is None, stop and return updatedAnswers
                        .fold(Try(updatedAnswers)) {
                          case BusinessType.Soleproprietor =>
                            (partnerDetails.solePropTitle,
                             partnerDetails.solePropFirstName,
                             partnerDetails.solePropMiddleName,
                             partnerDetails.solePropLastName
                            ) match {
                              // If SoleProp names are missing, stop and return updatedAnswers
                              case (Some(title), Some(firstName), middleName, Some(lastName)) =>
                                updatedAnswers.set(
                                  PartnerDetailsSoleProprietorPage(index),
                                  SoleProprietorName(
                                    title      = title,
                                    firstName  = firstName,
                                    middleName = middleName,
                                    lastName   = lastName
                                  )
                                )
                              // SoleProp names are missing
                              case _ => Try(updatedAnswers)
                            }
                          // BusinessType is missing
                          case _ =>
                            updatedAnswers.setIfDefined(PartnerDetailsBusinessNamePage(index), partnerDetails.businessName)
                        }

  } yield updatedAnswers

}

trait PartnerDetailsDataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
