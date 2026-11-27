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
import models.UserAnswers
import models.groupmembers.{GroupMember, GroupMembers}
import models.requests.{DataRequest, OptionalDataRequest}
import pages.groupmembers.*
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

class GroupMembersDataRequiredActionImpl @Inject() (
  val sessionRepository: SessionRepository,
  val gamblingConnector: GamblingConnector,
  clock: Clock
)(implicit val executionContext: ExecutionContext)
    extends GroupMembersDataRequiredAction
    with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    request.userAnswers match {
      case None =>
        logger.info(s"User Answers not found. Populating User Answers to id ${request.mgdRegNum}")

        given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        val answers = UserAnswers(request.mgdRegNum)
        saveUserAnswersToSessionAndRedirect(answers, request)

      case Some(userAnswers) =>
        if (userAnswers.isLoaded(GroupMembersSection)) {
          logger.info(s"Group Members already loaded for id ${userAnswers.id}")

          Future.successful(Right(DataRequest(request.request, request.mgdRegNum, userAnswers)))
        } else {
          logger.info(s"User Answers found with id ${userAnswers.id}. Loading Group Members")

          given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
          saveUserAnswersToSessionAndRedirect(userAnswers, request)
        }
    }
  }

  private def saveUserAnswersToSessionAndRedirect[A](answers: UserAnswers, request: OptionalDataRequest[A])(using HeaderCarrier) = {
    gamblingConnector.getGroupMembers(answers.id) flatMap { groupMembers =>

      setGroupMembers(groupMembers, answers) map { updatedAnswers =>
        logger.info("User Answers updated with Group Members. Saving User Answers")
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

  // Every member is loaded, also the members who have left the group, so that the original answers hold the section
  // as the backend returned it. Which members a screen shows is decided by the screen.
  private def setGroupMembers(groupMembers: GroupMembers, answers: UserAnswers): Try[UserAnswers] = {
    logger.info("Setting User Answers for Group Members")
    val loadedAnswers = answers.withSectionLoaded(GroupMembersSection, Instant.now(clock))
    groupMembers.groupMembers.foldLeft(Try(loadedAnswers)) { (userAnswers, groupMember) =>
      userAnswers.flatMap(setGroupMember(groupMember, _))
    }
  }

  // The registration number is the id of the answers, and the business partner number is the key of the member, so
  // neither is stored as a value. The system date is not an answer of the section.
  private def setGroupMember(groupMember: GroupMember, answers: UserAnswers): Try[UserAnswers] = {
    val businessPartnerNumber = groupMember.businessPartnerNumber
    for {
      updatedAnswers <- answers.setOriginalIfDefined(GroupMemberBusinessNamePage(businessPartnerNumber), groupMember.businessName)
      updatedAnswers <- updatedAnswers.setOriginalIfDefined(GroupMemberCrnPage(businessPartnerNumber), groupMember.crn)
      updatedAnswers <-
        updatedAnswers.setOriginalIfDefined(GroupMemberForeignCorporateRefPage(businessPartnerNumber), groupMember.foreignCorporateRef)
      updatedAnswers <-
        updatedAnswers.setOriginalIfDefined(GroupMemberIsRepMemberOfGroupPage(businessPartnerNumber), groupMember.isRepMemberOfGroup)
      updatedAnswers <- updatedAnswers.setOriginalIfDefined(GroupMemberDateOfJoiningPage(businessPartnerNumber), groupMember.dateOfJoining)
      updatedAnswers <- updatedAnswers.setOriginalIfDefined(GroupMemberDateOfLeavingPage(businessPartnerNumber), groupMember.dateOfLeaving)
      updatedAnswers <- updatedAnswers.setOriginalIfDefined(GroupMemberEmailAddressPage(businessPartnerNumber), groupMember.emailAddr)
    } yield updatedAnswers
  }
}

trait GroupMembersDataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
