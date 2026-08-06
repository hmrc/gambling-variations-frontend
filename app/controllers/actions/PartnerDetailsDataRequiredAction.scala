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
import pages.partnerdetails.{PartnerDetailsAddress1Page, PartnerDetailsAddress2Page, PartnerDetailsAddress3Page, PartnerDetailsAddress4Page, PartnerDetailsAdiPage, PartnerDetailsBusinessEmailPage, PartnerDetailsBusinessNamePage, PartnerDetailsContactNumberPage, PartnerDetailsCorrespondenceDetailsSectionPage, PartnerDetailsCountryOfIncorporation, PartnerDetailsCountryPage, PartnerDetailsCrnPage, PartnerDetailsDateOfBirthPage, PartnerDetailsDateOfIncorporation, PartnerDetailsDateOfJoiningPage, PartnerDetailsDateOfLeavingPage, PartnerDetailsFaxNumberPage, PartnerDetailsForeignCorporateRefPage, PartnerDetailsIomOrCiPage, PartnerDetailsMobilePhoneNumberPage, PartnerDetailsNinoPage, PartnerDetailsPage, PartnerDetailsPhoneNumberPage, PartnerDetailsPostcodePage, PartnerDetailsSolePropFirstNamePage, PartnerDetailsSolePropLastNamePage, PartnerDetailsSolePropMiddleNamePage, PartnerDetailsSolePropTitlePage, PartnerDetailsSoleProprietorPage, PartnerDetailsTradingNamePage, PartnerDetailsUtrPage, PartnerDetailsVrnPage}
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

  private def buildPartnerDetails(partnerDetails: PartnerDetails, index: Int, userAnswers: Try[UserAnswers]) = for {
    updatedAnswers <- userAnswers
    updatedAnswers <- updatedAnswers.set(PartnerDetailsPage(index), partnerDetails.mgdRegNumber)
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.dateOfJoining, PartnerDetailsDateOfJoiningPage(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.dateOfLeaving, PartnerDetailsDateOfLeavingPage(index))

//      updatedAnswers <- setIfDefined(updatedAnswers, soleProprietor, PartnerDetailsSoleProprietorPage(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.solePropTitle, PartnerDetailsSolePropTitlePage(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.solePropFirstName, PartnerDetailsSolePropFirstNamePage(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.solePropMiddleName, PartnerDetailsSolePropMiddleNamePage(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.solePropLastName, PartnerDetailsSolePropLastNamePage(index))

//      updatedAnswers <- {
//        address.flatMap(_.postcode) match {
//          case Some(_) => setIfDefined(updatedAnswers, address, PartnerDetailsCorrespondenceAddressUkPage(index))
//          case None    => setIfDefined(updatedAnswers, address, PartnerDetailsCorrespondenceAddressNonUkPage(index))
//        }
//      }
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.address1, PartnerDetailsAddress1Page(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.address2, PartnerDetailsAddress2Page(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.address3, PartnerDetailsAddress3Page(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.address4, PartnerDetailsAddress4Page(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.postcode, PartnerDetailsPostcodePage(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.country, PartnerDetailsCountryPage(index))

//      updatedAnswers <- setIfDefined(updatedAnswers, contactNumber, PartnerDetailsContactNumberPage(index))

    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.phoneNumber, PartnerDetailsPhoneNumberPage(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.mobilePhoneNumber, PartnerDetailsMobilePhoneNumberPage(index))

//      updatedAnswers <- updatedAnswers.set(PartnerDetailsCorrespondenceDetailsSectionPage(index), correspondenceDetails)

    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.adi, PartnerDetailsAdiPage(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.iomOrCiFlag, PartnerDetailsIomOrCiPage(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.faxNumber, PartnerDetailsFaxNumberPage(index))
    updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.emailAddress, PartnerDetailsBusinessEmailPage(index))

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

  private def setPartnerDetails(partnersDetails: PartnersDetails, answers: UserAnswers): Try[UserAnswers] = partnersDetails.partners.zipWithIndex
    .foldLeft(Try(answers)) { case (userAnswers, (partnerDetails, index)) =>
      buildPartnerDetails(partnerDetails, index, userAnswers)
    }

}

trait PartnerDetailsDataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
