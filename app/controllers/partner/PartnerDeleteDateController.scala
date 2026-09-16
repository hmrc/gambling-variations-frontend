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

package controllers.partner

import connectors.GamblingConnector
import controllers.actions.*
import forms.partner.PartnerDeleteDateFormProvider
import models.requests.DataRequest
import models.{BusinessDetails, Mode}
import pages.partnerdetails.{ChosenPartnerToRemovePage, PartnerDetailsBusinessNamePage, PartnerDetailsDateOfLeavingPage, PartnerDetailsTradingNamePage}
import play.api.i18n.{I18nSupport, Lang, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.*
import views.html.partner.PartnerDeleteDateView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerDeleteDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerDeleteDateFormProvider,
  gamblingConnector: GamblingConnector,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDeleteDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      gamblingConnector
        .getBusinessDetails(request.mgdRegNum)
        .map { businessDetails =>

          implicit val lang: Lang = messagesApi.preferred(request).lang

          val registrationDate = getRegistrationDate(businessDetails)
          val latestDate = calculateLatestDate(registrationDate)
          val index = getPartnerIndex
          val partnerName = getPartnerName(index)

          val form = formProvider(registrationDate)

          val preparedForm =
            request.userAnswers
              .get(PartnerDetailsDateOfLeavingPage(index))
              .fold(form)(form.fill)

          Ok(
            view(
              preparedForm,
              mode,
              registrationDate.format(dateTimeFormat()),
              latestDate.format(dateTimeHintFormat),
              partnerName
            )
          )
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      val index = getPartnerIndex

      gamblingConnector
        .getBusinessDetails(request.mgdRegNum)
        .flatMap { businessDetails =>

          implicit val lang: Lang = messagesApi.preferred(request).lang

          val registrationDate = getRegistrationDate(businessDetails)
          val latestDate = calculateLatestDate(registrationDate)
          val partnerName = getPartnerName(index)

          formProvider(registrationDate)
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      mode,
                      registrationDate.format(dateTimeFormat()),
                      latestDate.format(dateTimeHintFormat),
                      partnerName
                    )
                  )
                ),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(
                      request.userAnswers.set(
                        PartnerDetailsDateOfLeavingPage(index),
                        value
                      )
                    )

                  _ <- sessionRepository.set(updatedAnswers)

                } yield Redirect(
                  routes.PartnerCheckConfirmRemoveDateController.onPageLoad()
                )
            )
        }
    }

  private def getRegistrationDate(
    businessDetails: BusinessDetails
  ): LocalDate =
    businessDetails.dateOfRegistration.getOrElse(
      throw new RuntimeException("No registration date found")
    )

  private def calculateLatestDate(
    registrationDate: LocalDate
  ): LocalDate = {
    val currentDate = LocalDate.now()

    if (registrationDate.isAfter(currentDate)) {
      registrationDate.plusDays(15)
    } else {
      currentDate.plusDays(15)
    }
  }

  private def getPartnerIndex(implicit
    request: DataRequest[AnyContent]
  ): Int =
    request.userAnswers
      .get(ChosenPartnerToRemovePage)
      .getOrElse(
        throw new RuntimeException("No selected partner for removal")
      )

  private def getPartnerName(
    index: Int
  )(implicit request: DataRequest[AnyContent]): String =
    request.userAnswers
      .get(PartnerDetailsTradingNamePage(index))
      .orElse(
        request.userAnswers.get(
          PartnerDetailsBusinessNamePage(index)
        )
      )
      .getOrElse(request.mgdRegNum)
}
