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
import forms.licencespremises.OtherLicencesAndPermitsGBFormProvider
import models.{Mode, NormalMode, UserAnswers}
import models.licencespremises.*
import models.licencespremises.OtherLicencesAndPermitsGB.*
import viewmodels.OtherLicencesAndPermitsViewModel.*
import navigation.Navigator
import pages.licencespremises.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.OtherLicencesAndPermitsViewModel
import views.html.licencespremises.OtherLicencesAndPermitsGBView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class OtherLicencesAndPermitsGBController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: LicencesPremisesDataRequiredAction,
  formProvider: OtherLicencesAndPermitsGBFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: OtherLicencesAndPermitsGBView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val preparedForm = form.fill(getSelectedLicencesAndPermits(request.userAnswers))
    Ok(view(preparedForm, mode, OtherLicencesAndPermitsViewModel(preparedForm)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val ua: UserAnswers = request.userAnswers

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, OtherLicencesAndPermitsViewModel(formWithErrors)))),
        values =>
          for {
            updatedAnswers <- Future.fromTry(updateValuesAndCombine(values, ua))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(OtherLicencesAndPermitsGBPage, NormalMode, ua))
      )
  }

  private def getSelectedLicencesAndPermits(ua: UserAnswers): Set[OtherLicencesAndPermitsGB] = {
    val trueVal = "1"

    // iterates the types and their corresponding pages to check for a "1"
    // then converts to a set for the form to read
    mappedValuesWithPages.keys.filter(value => ua.get(mappedValuesWithPages(value)).contains(trueVal)).toSet
  }

  private def updateValuesAndCombine(formValues: Set[OtherLicencesAndPermitsGB], ua: UserAnswers): Try[UserAnswers] = {
    val trueVal = "1"
    val falseVal = "0"

    def checkIfFormDataContains(value: OtherLicencesAndPermitsGB): String = {
      if (formValues.contains(value)) trueVal else falseVal
    }

    if (formValues.contains(noOtherLicencesAndPermits)) {
      // sets all values to 0 if the checkbox for noOtherLicencesAndPermits is selected
      for {
        ua <- ua.set(LicenceClubGamingPage, falseVal)
        ua <- ua.set(ClubLicencePage, falseVal)
        ua <- ua.set(LicenceClubPremisesPage, falseVal)
        ua <- ua.set(LicenceFamilyEntertainmentPage, falseVal)
        ua <- ua.set(LicenceLocalAuthorityPage, falseVal)
        ua <- ua.set(LicenceOnPremisesPage, falseVal)
        ua <- ua.set(LicencePrizeGamingPage, falseVal)
      } yield ua
    } else {
      for {
        ua <- ua.set(LicenceClubPremisesPage, checkIfFormDataContains(clubGaming))
        ua <- ua.set(ClubLicencePage, checkIfFormDataContains(clubMachine))
        ua <- ua.set(LicenceClubPremisesPage, checkIfFormDataContains(clubPremises))
        ua <- ua.set(LicenceFamilyEntertainmentPage, checkIfFormDataContains(familyEntertainment))
        ua <- ua.set(LicenceLocalAuthorityPage, checkIfFormDataContains(localAuthority))
        ua <- ua.set(LicenceOnPremisesPage, checkIfFormDataContains(onPremises))
        ua <- ua.set(LicencePrizeGamingPage, checkIfFormDataContains(prizeGaming))
      } yield ua
    }
  }
}
