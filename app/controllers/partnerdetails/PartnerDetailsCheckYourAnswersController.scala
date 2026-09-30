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

package controllers.partnerdetails

import controllers.actions.*
import controllers.routes
import models.{Mode, UserAnswers}
import pages.BusinessNumberOrIndex
import pages.partnerdetails.*
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.partnerdetails.CheckPartnerDetailsViewModel.from
import views.html.partnerdetails.PartnerDetailsCheckYourAnswersView

import javax.inject.Inject

class PartnerDetailsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsCheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(index: String, mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      val answers: UserAnswers = request.userAnswers
      parsePartner(index, answers).fold(
        error => {
          logger.warn(error)
          Redirect(routes.SystemErrorController.onPageLoad())
        },
        partner => Ok(view(from(answers, partner.ref, partner.isNew, partner.isSubmitted)))
      )
    }

  private def parsePartner(index: String, userAnswers: UserAnswers): Either[String, Partner] =
    index.toIntOption match {
      case Some(i) if i >= 0                     => Right(NewPartner(i, userAnswers.get(PartnerDetailsAddPartnerCompletedPage(i))))
      case None if index.matches("[A-Za-z0-9]+") => Right(ExistingPartner(index))
      case _                                     => Left(s"Invalid partner reference: $index")
    }
}

sealed trait Partner {
  def ref: BusinessNumberOrIndex
  def isNew: Boolean
  def isSubmitted: Option[Boolean]
}

final case class NewPartner(index: Int, isSubmitted: Option[Boolean]) extends Partner {
  val ref: BusinessNumberOrIndex = index
  val isNew: Boolean = true
}

final case class ExistingPartner(businessPartnerNumber: String) extends Partner {
  val ref: BusinessNumberOrIndex = businessPartnerNumber
  val isNew: Boolean = false
  val isSubmitted: Option[Boolean] = None
}
