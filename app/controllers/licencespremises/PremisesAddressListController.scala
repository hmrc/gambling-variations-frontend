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
import models.{Mode, NormalMode}
import pages.licencespremises.{AddPremisesAddressPage, PremisesDetailsPage}
import forms.licencespremises.PremisesAddressListFormProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.licencespremises.PremisesAddressListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PremisesAddressListController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: LicencesPremisesDataRequiredAction,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  formProvider: PremisesAddressListFormProvider,
  view: PremisesAddressListView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()
  private val maxPremisesAddresses = 100

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm =
      request.userAnswers
        .get(AddPremisesAddressPage)
        .fold(form)(form.fill)

    request.userAnswers
      .get(PremisesDetailsPage)
      .fold(
        Redirect(routes.AccessDeniedController.onPageLoad())
      )(premisesList =>
        val addressList = premisesList.premises
        Ok(view(preparedForm, NormalMode, addressList, maxPremisesAddresses))
      )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(PremisesDetailsPage)
      .fold(
        Future.successful(Redirect(routes.AccessDeniedController.onPageLoad()))
      )(premisesList =>
        val addressList = premisesList.premises
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, NormalMode, addressList, maxPremisesAddresses))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddPremisesAddressPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(routes.SystemErrorController.onPageLoad().url)
          )
      )
  }
}
