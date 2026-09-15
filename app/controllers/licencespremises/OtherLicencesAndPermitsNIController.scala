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
import controllers.*
import forms.licencespremises.OtherLicencesAndPermitsNIFormProvider
import models.{Mode, NormalMode, UserAnswers}
import models.licencespremises.*
import models.licencespremises.OtherLicencesAndPermitsNI.*
import navigation.Navigator
import pages.licencespremises.*
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.OtherLicencesAndPermitsNIViewModel
import views.html.licencespremises.OtherLicencesAndPermitsNIView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class OtherLicencesAndPermitsNIController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: LicencesPremisesDataRequiredAction,
  formProvider: OtherLicencesAndPermitsNIFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: OtherLicencesAndPermitsNIView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Set[OtherLicencesAndPermitsNI]] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val preparedForm = form.fill(getSelectedLicencesAndPermits(request.userAnswers))
    Ok(view(preparedForm, mode, OtherLicencesAndPermitsNIViewModel(preparedForm)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val ua: UserAnswers = request.userAnswers

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, OtherLicencesAndPermitsNIViewModel(formWithErrors)))),
        values =>
          for {
            updatedAnswers <- Future.fromTry(updateValuesAndCombine(values, ua))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(OtherLicencesAndPermitsNIPage, NormalMode, ua))
      )
  }

  private def updateValuesAndCombine(formValues: Set[OtherLicencesAndPermitsNI], ua: UserAnswers): Try[UserAnswers] = {
    val trueVal = "1"
    val falseVal = "0"

    def returnIfSelected(value: OtherLicencesAndPermitsNI): String = {
      if (formValues.contains(value)) trueVal else falseVal
    }

    for {
      ua <- ua.set(LicenceAmusementPage, returnIfSelected(amusement))
      ua <- ua.set(LicenceBingoPage, returnIfSelected(bingo))
      ua <- ua.set(LicenceBookmakingPage, returnIfSelected(bookmaking))
      ua <- ua.set(LicenceServeAlcoholPage, returnIfSelected(serveAlcohol))
      ua <- ua.set(LicenceRegCertPage, returnIfSelected(regCert))
      ua <- ua.set(NoOtherLicencesAndPermitsNIPage, returnIfSelected(noOtherLicencesAndPermits))
    } yield ua
  }
}
