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

package controllers.licencespremises

import controllers.actions.*
import controllers.routes
import forms.licencespremises.RemovePremisesAddressFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.licencespremises.*
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.licencespremises.RemovePremisesAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class RemovePremisesAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: LicencesPremisesDataRequiredAction,
  formProvider: RemovePremisesAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemovePremisesAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    request.userAnswers.get(ChosenPremisesAddressPage) match {
      case Some(address) =>
        Ok(view(form, mode, address))
      case None =>
        Redirect(routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(ChosenPremisesAddressPage)
        .map { chosenPremisesAddress =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, chosenPremisesAddress))),
              value =>
                for {
                  updatedAnswers                   <- Future.fromTry(updateUserAnswers(request.userAnswers, value))
                  updatedAnswersWithSubmitted      <- Future.fromTry(updatedAnswers.set(LicencesPremisesDetailsSubmittedPage, true))
                  updatedAnswersWithAddressRemoved <- Future.fromTry(updatedAnswersWithSubmitted.remove(ChosenPremisesAddressPage))
                  finalAnswer                      <- Future.fromTry(updatedAnswersWithAddressRemoved.set(LicencesPremisesDetailsChangesPage, value))
                  _                                <- sessionRepository.set(finalAnswer)
                } yield Redirect(navigator.nextPage(RemovePremisesAddressPage, mode, finalAnswer))
            )
            .recover { case ex =>
              Redirect(routes.SystemErrorController.onPageLoad())
            }
        }
        .getOrElse(Future.successful(Redirect(routes.SystemErrorController.onPageLoad())))
    }

  private def updateUserAnswers(userAnswers: UserAnswers, value: Boolean): Try[UserAnswers] = {
    for {
      updatedAnswers <- userAnswers.set(RemovePremisesAddressPage, value)

      result <- {
        if (!value) {
          Try(updatedAnswers)
        } else {

          val chosenAddress = userAnswers.get(ChosenPremisesAddressPage)
          val premisesDetails = userAnswers.get(PremisesDetailsPage)

          (chosenAddress, premisesDetails) match {

            case (Some(address), Some(details)) =>
              val updatedPremises = details.premises.filterNot { premises =>
                premises.address1.contains(address.address1) &&
                premises.address2 == address.address2 &&
                premises.address3 == address.address3 &&
                premises.address4 == address.address4 &&
                premises.postcode == address.postcode
              }

              if (updatedPremises.size == details.premises.size) {
                Failure(new RuntimeException("Chosen premises address was not found in premises details"))
              } else {
                val updatedDetails = details.copy(premises = updatedPremises)
                updatedAnswers.set(PremisesDetailsPage, updatedDetails)
              }
            case _ =>
              Failure(new RuntimeException("Chosen premises address or premises details are missing"))
          }
        }
      }
    } yield result
  }
}
