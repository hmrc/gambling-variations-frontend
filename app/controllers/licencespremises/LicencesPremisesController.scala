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
import forms.licencespremises.LicencesPremisesFormProvider
import javax.inject.Inject
import models.NormalMode
import models.licencespremises.LicencesAndPremisesRadioOptions
import models.licencespremises.LicencesAndPremisesRadioOptions.ByPost
import models.licencespremises.LicencesPremisesAnswers.*
import navigation.Navigator
import pages.licencespremises.LicencesPremisesPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.licencespremises.LicencesPremisesView

import scala.concurrent.{ExecutionContext, Future}

class LicencesPremisesController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: LicencesPremisesDataRequiredAction,
  formProvider: LicencesPremisesFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: LicencesPremisesView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form: Form[LicencesAndPremisesRadioOptions] = formProvider()

  def onPageLoad(): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm = form.fill(request.userAnswers.provideAddressesAnswer)

    Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          // Choosing by post would remove the premises already provided, so the user must confirm that first and nothing is saved until then
          if (value == ByPost && request.userAnswers.premisesCount > 0) {
            Future.successful(Redirect(routes.RemovePremisesDetailsYesNoController.onPageLoad()))
          } else {
            for {
              answersWithValue <- Future.fromTry(request.userAnswers.set(LicencesPremisesPage, value))
              updatedAnswers   <- Future.fromTry(answersWithValue.withLicencesPremisesFlags(isChanged = false))
              _                <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(LicencesPremisesPage, NormalMode, updatedAnswers))
          }
      )
  }
}
