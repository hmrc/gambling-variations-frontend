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
import models.BusinessChangeAddrOption.reads
import models.BusinessType.Soleproprietor
import models.requests.{DataRequest, OptionalDataRequest}
import models.{BusinessPartnerDetails, BusinessType, CorrespondenceDetails, PartnerDetails, PartnersDetails, SoleProprietorName, SoleProprietorPartnerDetails, UserAnswers}
import pages.*
import pages.partnerdetails.*
import play.api.Logging
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsObject, Writes}
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

      case Some(userAnswers: UserAnswers) =>
        logger.info(s"User Answers found with id ${userAnswers.id}")

        userAnswers.get(PartnerDetailsPage) map { _ =>
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
  ): Try[UserAnswers] = partnersDetails.partners
    .filterNot(_.dateOfLeaving.exists(_.isBefore(LocalDate.now())))
    .foldLeft(Try(answers)) { case (userAnswers, partnerDetails) =>
      buildPartnerDetails(partnerDetails, partnerDetails.businessPartnerNumber, userAnswers)
    }

  private def buildPartnerDetails(partnerDetails: PartnerDetails, partnerBusinessNumber: String, userAnswers: Try[UserAnswers]) = for {
    updatedAnswers <- userAnswers

    updatedAnswers <- updatedAnswers.set(PartnerDetailsMgdRegNumberPage(partnerBusinessNumber), partnerDetails.mgdRegNumber)
    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsTradingNamePage(partnerBusinessNumber), partnerDetails.tradingName)
    updatedAnswers <- updatedAnswers.set(PartnerDetailsBusinessTypePage(partnerBusinessNumber), partnerDetails.businessType)

    updatedAnswers <-
      updatedAnswers.set(PartnerDetailsBusinessPartnerNumberPage(partnerBusinessNumber), partnerDetails.businessPartnerNumber)
    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsDateOfJoiningPage(partnerBusinessNumber), partnerDetails.dateOfJoining)
    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsDateOfLeavingPage(partnerBusinessNumber), partnerDetails.dateOfLeaving)

    updatedAnswers <-
      updatedAnswers.setIfDefined(PartnerDetailsCorrespondenceDetailsSectionPage(partnerBusinessNumber), partnerDetails.correspondenceDetails)

    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsDateOfIncorporationPage(partnerBusinessNumber), partnerDetails.dateOfIncorporation)
    updatedAnswers <-
      updatedAnswers.setIfDefined(PartnerDetailsCountryOfIncorporationPage(partnerBusinessNumber), partnerDetails.countryOfIncorporation)
    updatedAnswers <-
      updatedAnswers.setIfDefined(PartnerDetailsForeignCorporateReferencePage(partnerBusinessNumber), partnerDetails.foreignCorporateRef)

    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsDateOfBirthPage(partnerBusinessNumber), partnerDetails.dateOfBirth)
    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsNinoPage(partnerBusinessNumber), partnerDetails.nino)
    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsUtrPage(partnerBusinessNumber), partnerDetails.utr)
    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsVrnPage(partnerBusinessNumber), partnerDetails.vrn)
    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsCrnPage(partnerBusinessNumber), partnerDetails.crn)

    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsIsFutureLeaveDatePage(partnerBusinessNumber), partnerDetails.isFutureLeaveDate)
    updatedAnswers <- updatedAnswers.setIfDefined(PartnerDetailsIsFutureJoinDatePage(partnerBusinessNumber), partnerDetails.isFutureJoinDate)

    updatedAnswers <- partnerDetails.businessType match {
                        case Soleproprietor =>
                          val partnerDetailsSoleProp = partnerDetails.asInstanceOf[SoleProprietorPartnerDetails]
                          updatedAnswers.setIfDefined(PartnerDetailsSoleProprietorPage(partnerBusinessNumber),
                                                      partnerDetailsSoleProp.soleProprietorName
                                                     )
                        case _ =>
                          val partnerDetailsBusiness = partnerDetails.asInstanceOf[BusinessPartnerDetails]
                          updatedAnswers.setIfDefined(PartnerDetailsBusinessNamePage(partnerBusinessNumber), partnerDetailsBusiness.businessName)
                      }

  } yield updatedAnswers
}

trait PartnerDetailsDataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
