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

import config.FrontendAppConfig
import controllers.actions.*
import forms.partnerdetails.AddAnotherPartnerFormProvider
import models.{NormalMode, UserAnswers}
import pages.partnerdetails.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{PaginationResult, PaginationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PartnerUtils
import viewmodels.checkAnswers.partnerdetails.PartnerDetailsViewModel
import views.html.partnerdetails.PartnerDetailsView

import java.time.{LocalDate, ZoneOffset}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: AddAnotherPartnerFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsView,
  frontendAppConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val paginationService: PaginationService = PaginationService(
    recordsPerPage  = frontendAppConfig.partnersPerPage,
    maxRecords      = frontendAppConfig.maxPartners,
    maxVisiblePages = 5
  )

  def onPageLoad(page: Option[Int]): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      val selectedPage = page.getOrElse(1)

      val todayDate = LocalDate.now(ZoneOffset.UTC)
      val paginatedPartnerNumbers = fetchPaginatedPartnerDetails(request.userAnswers, todayDate, selectedPage)

      val viewModel =
        PartnerDetailsViewModel.from(
          paginatedPartnerNumbers.paginatedData,
          todayDate,
          request.userAnswers,
          frontendAppConfig
        )

      val errorMessage =
        if viewModel.showNoPartnersMessage then "partnerDetails.addPartner.error.required"
        else "partnerDetails.addAnotherPartner.error.required"

      val form = formProvider(errorMessage)

      val preparedForm =
        request.userAnswers
          .get(PartnerDetailsAddAnotherPartnerYesNoPage)
          .fold(form)(form.fill)

      Ok(
        view(
          preparedForm,
          viewModel,
          paginatedPartnerNumbers.paginationViewModel,
          selectedPage,
          paginatedPartnerNumbers.from,
          paginatedPartnerNumbers.to,
          paginatedPartnerNumbers.totalRecords
        )
      )
    }

  def onSubmit(page: Option[Int]): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      val selectedPage = page.getOrElse(1)

      val todayDate = LocalDate.now(ZoneOffset.UTC)
      val paginatedPartnerNumbers = fetchPaginatedPartnerDetails(request.userAnswers, todayDate, selectedPage)

      val viewModel =
        PartnerDetailsViewModel.from(
          paginatedPartnerNumbers.paginatedData,
          todayDate,
          request.userAnswers,
          frontendAppConfig
        )

      val errorMessage =
        if (viewModel.showNoPartnersMessage)
          "partnerDetails.addPartner.error.required"
        else
          "partnerDetails.addAnotherPartner.error.required"

      formProvider(errorMessage)
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  viewModel,
                  paginatedPartnerNumbers.paginationViewModel,
                  selectedPage,
                  paginatedPartnerNumbers.from,
                  paginatedPartnerNumbers.to,
                  paginatedPartnerNumbers.totalRecords
                )
              )
            ),
          value =>
            for {
              updatedAnswers <-
                Future.fromTry(
                  request.userAnswers.set(
                    PartnerDetailsAddAnotherPartnerYesNoPage,
                    value
                  )
                )

              _ <- sessionRepository.set(updatedAnswers)
            } yield {
              if (value) {
                val newPartnerIndex = PartnerUtils.findIndexForNewPartner(updatedAnswers)
                for {
                  updatedAnswers <- Future.fromTry(updatedAnswers.set(PartnerDetailsAddPartnerCompletedPage(newPartnerIndex), false))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield ()

                Redirect(
                  controllers.partnerdetails.routes.PartnerDetailsBusinessTypeController.onPageLoad(
                    newPartnerIndex.toString,
                    NormalMode
                  )
                )
              } else {
                Redirect(
                  controllers.routes.ChangeRegistrationDetailsController
                    .onPageLoad()
                )
              }
            }
        )
    }

  def onPartnerDetails(partnerNumber: String): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      Redirect(
        routes.PartnerDetailsController.onPageLoad(None)
      )
    }

  def onRemove(partnerNumber: String): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      for {
        updatedAnswers <-
          Future.fromTry(
            request.userAnswers.set(
              PartnerDetailsChosenPartnerToRemovePage,
              partnerNumber
            )
          )

        _ <- sessionRepository.set(updatedAnswers)

      } yield Redirect(
        controllers.partnerdetails.routes.PartnerDetailsDeleteDateController.onPageLoad()
      )
    }

  private def fetchPaginatedPartnerDetails(userAnswers: UserAnswers, todayDate: LocalDate, page: Int): PaginationResult = {
    val partnersPerPage = frontendAppConfig.partnersPerPage

    val completedNewPartners = PartnerUtils.getCompletedNewPartners(userAnswers)
    val existingPartners = getPresentExistingPartners(userAnswers, todayDate)

    paginationService
      .paginatePartnerDetails(completedNewPartners, existingPartners, page, partnersPerPage, routes.PartnerDetailsController.onPageLoad(None).url)
  }

  private def getPresentExistingPartners(userAnswers: UserAnswers, todayDate: LocalDate): Seq[String] = PartnerUtils
    .getExistingPartnersBusinessNumbers(userAnswers)
    .filter { partnerNumber =>
      val hasPartner =
        userAnswers
          .get(PartnerDetailsMgdRegNumberPage(partnerNumber))
          .isDefined

      val hasPastLeavingDate =
        userAnswers
          .get(PartnerDetailsDateOfLeavingPage(partnerNumber))
          .exists(_.isBefore(todayDate))

      hasPartner && !hasPastLeavingDate
      true
    }
}
