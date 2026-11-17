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
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.BusinessNumberOrIndex
import pages.partnerdetails.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
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

  private val paginationService: PaginationService =
    PaginationService(
      recordsPerPage  = frontendAppConfig.partnersPerPage,
      maxRecords      = frontendAppConfig.maxPartners,
      maxVisiblePages = 5
    )

  def onPageLoad(page: Option[Int]): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      val selectedPage = page.getOrElse(1)
      val todayDate = LocalDate.now(ZoneOffset.UTC)

      val allPartnerNumbers =
        getAllPartnerNumbers(request.userAnswers)

      val paginatedPartnerNumbers =
        fetchPaginatedPartnerDetails(
          userAnswers = request.userAnswers,
          page        = selectedPage
        )

      val viewModel =
        PartnerDetailsViewModel.from(
          paginatedPartnerNumbers = paginatedPartnerNumbers.paginatedData,
          allPartnerNumbers       = allPartnerNumbers,
          todayDate               = todayDate,
          userAnswers             = request.userAnswers,
          frontendAppConfig       = frontendAppConfig
        )

      val errorMessage =
        if (viewModel.showNoPartnersMessage) {
          "partnerDetails.addPartner.error.required"
        } else {
          "partnerDetails.addAnotherPartner.error.required"
        }

      val form =
        formProvider(errorMessage)

      Ok(
        view(
          form,
          viewModel,
          paginatedPartnerNumbers.paginationViewModel,
          paginatedPartnerNumbers.currentPage,
          paginatedPartnerNumbers.totalPages,
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

      val allPartnerNumbers =
        getAllPartnerNumbers(request.userAnswers)

      val paginatedPartnerNumbers =
        fetchPaginatedPartnerDetails(
          userAnswers = request.userAnswers,
          page        = selectedPage
        )

      val viewModel =
        PartnerDetailsViewModel.from(
          paginatedPartnerNumbers = paginatedPartnerNumbers.paginatedData,
          allPartnerNumbers       = allPartnerNumbers,
          todayDate               = todayDate,
          userAnswers             = request.userAnswers,
          frontendAppConfig       = frontendAppConfig
        )

      val errorMessage =
        if (viewModel.showNoPartnersMessage) {
          "partnerDetails.addPartner.error.required"
        } else {
          "partnerDetails.addAnotherPartner.error.required"
        }

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
                  paginatedPartnerNumbers.currentPage,
                  paginatedPartnerNumbers.totalPages,
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

              _ <-
                sessionRepository.set(updatedAnswers)

              result <-
                if (value) {
                  addNewPartner(updatedAnswers)
                } else {
                  Future.successful(
                    Redirect(
                      controllers.routes.ChangeRegistrationDetailsController
                        .onPageLoad()
                    )
                  )
                }
            } yield result
        )
    }

  def onPartnerDetails(
    partnerNumber: String,
    mode: Mode
  ): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      Redirect(
        routes.PartnerDetailsController.onPageLoad(None)
      )
    }

  def onRemove(
    partnerNumber: String,
    mode: Mode
  ): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      mode match {
        case NormalMode =>
          // TODO: Journey for removing new partners
          Future.successful(
            Redirect(
              routes.PartnerDetailsController.onPageLoad(None)
            )
          )

        case CheckMode =>
          for {
            updatedAnswers <-
              Future.fromTry(
                request.userAnswers.set(
                  PartnerDetailsChosenPartnerToRemovePage,
                  partnerNumber
                )
              )

            _ <-
              sessionRepository.set(updatedAnswers)

          } yield {
            Redirect(
              routes.PartnerDetailsDeleteDateController
                .onPageLoad()
            )
          }
      }
    }

  private def addNewPartner(
    userAnswers: UserAnswers
  ): Future[Result] = {

    val newPartnerIndex =
      PartnerUtils.getNewPartnersSize(userAnswers)

    for {
      answersWithIncompletePartner <-
        Future.fromTry(
          userAnswers.set(
            PartnerDetailsAddPartnerCompletedPage(newPartnerIndex),
            false
          )
        )

      updatedAnswers <-
        Future.fromTry(
          answersWithIncompletePartner.set(
            PartnerDetailsMgdRegNumberPage(newPartnerIndex),
            answersWithIncompletePartner.id
          )
        )

      _ <-
        sessionRepository.set(updatedAnswers)

    } yield {
      Redirect(
        routes.PartnerDetailsBusinessTypeController
          .onPageLoad(
            newPartnerIndex.toString,
            NormalMode
          )
      )
    }
  }

  private def fetchPaginatedPartnerDetails(
    userAnswers: UserAnswers,
    page: Int
  ): PaginationResult = {

    val completedNewPartners =
      PartnerUtils.getCompletedNewPartners(userAnswers)

    val existingPartners =
      getPresentExistingPartners(userAnswers)

    paginationService.paginateAlphabeticallyPartnerDetails(
      completedNewPartners,
      existingPartners,
      userAnswers,
      page,
      routes.PartnerDetailsController
        .onPageLoad(None)
        .url
    )
  }

  private def getPresentExistingPartners(
    userAnswers: UserAnswers
  ): Seq[String] =
    PartnerUtils
      .getExistingPartnersBusinessNumbers(userAnswers)
      .filter { partnerNumber =>
        userAnswers
          .get(
            PartnerDetailsMgdRegNumberPage(partnerNumber)
          )
          .isDefined
      }
      .toSeq

  private def getAllPartnerNumbers(
    userAnswers: UserAnswers
  ): Seq[BusinessNumberOrIndex] = {

    val completedNewPartners: Seq[BusinessNumberOrIndex] =
      PartnerUtils.getCompletedNewPartners(userAnswers)

    val existingPartners: Seq[BusinessNumberOrIndex] =
      getPresentExistingPartners(userAnswers)

    completedNewPartners ++ existingPartners
  }
}
