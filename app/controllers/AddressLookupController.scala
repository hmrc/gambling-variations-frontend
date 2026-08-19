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

package controllers

import controllers.actions.*
import models.Address
import pages.{CorrespondenceAddressUkPage, CorrespondenceDetailsSubmittedPage, isleMOrChannelFlagPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AddressLookupService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressLookupController @Inject() (
  override val messagesApi: MessagesApi,
  addressLookupService: AddressLookupService,
  sessionRepository: SessionRepository,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: CorrespondenceDetailsDataRequiredAction,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def initialise(): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      addressLookupService.initJourney().map(Redirect(_))
    }

  def callback(id: String): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      for {
        address <- addressLookupService.retrieveAddress(id)
        userAnswersWithAddress <- Future.fromTry(
                                    request.userAnswers
                                      .set(CorrespondenceAddressUkPage, address)
                                      .flatMap(_.set(isleMOrChannelFlagPage, isIomOrCiAddress(address).toString))
                                      .flatMap(_.set(CorrespondenceDetailsSubmittedPage, true))
                                  )
        _ <- sessionRepository.set(userAnswersWithAddress)
      } yield Redirect(routes.CheckCorrespondenceDetailsController.onPageLoad())
    }

  private def isIomOrCiAddress(address: Address): Boolean =
    address.postcode.exists { postcode =>
      val normalised = postcode.trim.toUpperCase

      normalised.startsWith("IM") ||
      normalised.startsWith("JE") ||
      normalised.startsWith("GY")
    }
}
