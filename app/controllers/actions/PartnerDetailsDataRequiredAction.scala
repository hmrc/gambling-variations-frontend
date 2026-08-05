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
import models.{Address, ContactNumber, CorrespondenceDetails, MgdTradeDetails, PartnerDetails, PartnersDetails, SoleProprietorName, UserAnswers}
import pages.*
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

        // TODO this checks minimum parameters needed to make it run
        // in this case, an email
        userAnswers.get(PartnerDetailsBusinessEmailPage) map { _ =>
          Future.successful(Right(DataRequest(request.request, request.mgdRegNum, userAnswers)))
        } getOrElse {
          given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
          saveUserAnswersToSessionAndRedirect(userAnswers, request)
        }
    }
  }

  private def saveUserAnswersToSessionAndRedirect[A](answers: UserAnswers, request: OptionalDataRequest[A])(using HeaderCarrier) = {
    gamblingConnector.getPartnerDetails(answers.id) flatMap { mgdContactDetails =>
      // TODO just for testing, head of the partners details
      setPartnerDetails(mgdContactDetails.partners.head, answers) map { updatedAnswers =>
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

  private def setPartnerDetails(partnerDetails: PartnerDetails, answers: UserAnswers): Try[UserAnswers] = {
    logger.info("Setting User Answers for Partner Details")

    val address: Option[Address] = partnerDetails.address1.map(address1 =>
      Address(
        address1 = address1,
        address2 = partnerDetails.address2,
        address3 = partnerDetails.address3,
        address4 = partnerDetails.address4,
        postcode = partnerDetails.postcode,
        country  = partnerDetails.country
      )
    )

    // TODO this makes no sense, it should be an option
    val soleProprietor: Option[SoleProprietorName] =
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


    val contactNumber: Option[ContactNumber] = (partnerDetails.phoneNumber, partnerDetails.mobilePhoneNumber) match {
      case (None, None) => None
      case (a, b) => Some(ContactNumber(a, b))
    }
//    val contactNumber = ContactNumber(
//      phoneNumber = partnerDetails.phoneNumber,
//      mobilePhoneNumber = partnerDetails.mobilePhoneNumber
//    )

    val correspondenceDetails = CorrespondenceDetails(
      mgdRegNumber          = partnerDetails.mgdRegNumber,
      nameLine1             = None, //TODO - what would it be?
      nameLine2             = None, //TODO - what would it be?
      correspondenceAddress = address,
      additionalInformation = partnerDetails.adi, //TODO is adi = additional information
      iomOrCiFlag           = partnerDetails.iomOrCiFlag,
      contactNumber         = contactNumber,
      faxNumber             = partnerDetails.faxNumber,
      emailAddr             = partnerDetails.emailAddress
    )

    for {
      updatedAnswers <- answers.set(PartnerDetailsPage, partnerDetails.mgdRegNumber) // TODO ✅

      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.dateOfJoining, PartnerDetailsDateOfJoiningPage) // TODO ✅
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.dateOfLeaving, PartnerDetailsDateOfLeavingPage) // TODO ✅

      updatedAnswers <- setIfDefined(updatedAnswers, soleProprietor, SoleProprietorPage) // TODO ✅ BUT Option?
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.solePropTitle, SoleProprietorPage)// TODO ✅
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.solePropFirstName, PartnerDetailsBusinessEmailPage)// TODO ✅
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.solePropMiddleName, PartnerDetailsBusinessEmailPage)// TODO ✅
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.solePropLastName, PartnerDetailsBusinessEmailPage)// TODO ✅

      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.businessName, BusinessNamePage)
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.tradingName, PartnerDetailsBusinessEmailPage)
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.dateOfBirth, PartnerDetailsBusinessEmailPage)
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.nino, PartnerDetailsBusinessEmailPage)
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.utr, PartnerDetailsBusinessEmailPage)
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.vrn, PartnerDetailsBusinessEmailPage)
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.crn, PartnerDetailsBusinessEmailPage)
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.dateOfIncorporation, PartnerDetailsBusinessEmailPage)
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.countryOfIncorporation, PartnerDetailsBusinessEmailPage)
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.foreignCorporateRef, PartnerDetailsBusinessEmailPage)

      // TODO this is re-using some structure already existing
      updatedAnswers <- {
        address.flatMap(_.postcode) match {
          case Some(_) => setIfDefined(updatedAnswers, address, CorrespondenceAddressUkPage) // TODO ✅
          case None    => setIfDefined(updatedAnswers, address, CorrespondenceAddressNonUkPage) // TODO ✅
        }
      }
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.address1, PartnerDetailsBusinessEmailPage)// TODO ✅
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.address2, PartnerDetailsBusinessEmailPage)// TODO ✅
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.address3, PartnerDetailsBusinessEmailPage)// TODO ✅
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.address4, PartnerDetailsBusinessEmailPage)// TODO ✅
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.postcode, PartnerDetailsBusinessEmailPage)// TODO ✅
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.country, PartnerDetailsBusinessEmailPage)// TODO ✅

      //TODO so this one makes less sense to me because there is an action already that takes Correspondence and it breaks it down to individual pieces.
      updatedAnswers <- setIfDefined(updatedAnswers, correspondenceDetails, CorrespondenceDetailsSectionPage) 
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.adi, PartnerDetailsBusinessEmailPage)
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.iomOrCiFlag, PartnerDetailsBusinessEmailPage)

//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.phoneNumber, PartnerDetailsPhoneNumberPage) // TODO ✅
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.mobilePhoneNumber, PartnerDetailsMobileNumberPage) // TODO ✅
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.faxNumber, PartnerDetailsFaxNumberPage) // TODO ✅
//      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.emailAddress, PartnerDetailsBusinessEmailPage) // TODO ✅

      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.isFutureLeaveDate, PartnerDetailsBusinessEmailPage)
      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.isFutureJoinDate, PartnerDetailsBusinessEmailPage)

      updatedAnswers <- setIfDefined(updatedAnswers, partnerDetails.businessType, BusinessTypePage) // TODO ✅
    } yield updatedAnswers
  }

}

trait PartnerDetailsDataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
