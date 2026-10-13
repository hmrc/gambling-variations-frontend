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

package controllers.returnperiods

import controllers.actions.*
import controllers.routes
import forms.returnperiods.{NoReturnPeriodsSetFormProvider, NonStandardLastReturnPeriodsFormProvider, NonStandardNotLastReturnPeriodsFormProvider, StandardReturnPeriodsFormProvider}
import models.requests.DataRequest
import models.{GamblingReturnPeriods, Mode, NoReturnPeriodsSet, NonStandardReturnPeriodsOptions, StandardReturnPeriodsOptions, UserAnswers}
import navigation.Navigator
import pages.QuestionPage
import pages.returnperiods.{GamblingReturnPeriodsPage, HasSwitchedToStandardValuesPage, NonStandardNotLastReturnPeriodsYesNoPage, NonStandardReturnPeriodsPage, ReturnPeriodsChoicePage, StandardReturnPeriodsPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{Reads, Writes}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.returnperiods.NonStandardReturnPeriodsViewModel
import views.html.returnperiods.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReturnPeriodsController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: GamblingReturnPeriodsDataRequiredAction,
  nonStandardNotLastPeriodFormProvider: NonStandardLastReturnPeriodsFormProvider,
  standardPeriodFormProvider: StandardReturnPeriodsFormProvider,
  noReturnPeriodsSetFormProvider: NoReturnPeriodsSetFormProvider,
  nonStandardNotLastReturnPeriodsFormProvider: NonStandardNotLastReturnPeriodsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  nonStandardReturnPeriodsView: NonStandardReturnPeriodsView,
  standardReturnPeriodsView: StandardReturnPeriodsView,
  noReturnPeriodsSetView: NoReturnPeriodsSetView,
  lockedReturnPeriodsSetView: LockedReturnPeriodsView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  import ReturnPeriodsController.*

  private val standardForm: Form[StandardReturnPeriodsOptions] = standardPeriodFormProvider()
  private val nonStandardLastForm: Form[NonStandardReturnPeriodsOptions] = nonStandardNotLastPeriodFormProvider()
  private val nonStandardNotLastForm: Form[Boolean] = nonStandardNotLastReturnPeriodsFormProvider()
  private val noPeriodsSetForm: Form[NoReturnPeriodsSet] = noReturnPeriodsSetFormProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    screenFor(request.userAnswers) match {
      case Some(NoPeriodsSet) =>
        Ok(noReturnPeriodsSetView(prepare(noPeriodsSetForm, ReturnPeriodsChoicePage), mode))

      case Some(LockedPeriods) =>
        Ok(lockedReturnPeriodsSetView())

      case Some(Standard(returnPeriodsId)) =>
        Ok(standardReturnPeriodsView(prepare(standardForm, StandardReturnPeriodsPage), mode, returnPeriodsId))

      case Some(NonStandard(viewModel)) =>
        Ok(nonStandardReturnPeriodsView(prepare(nonStandardLastForm, NonStandardReturnPeriodsPage), viewModel, mode))

      case Some(NonStandardNotLast(viewModel)) =>
        Ok(nonStandardReturnPeriodsView(prepare(nonStandardNotLastForm, NonStandardNotLastReturnPeriodsYesNoPage), viewModel, mode))
      case None =>
        Redirect(routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    screenFor(request.userAnswers) match {
      case Some(NoPeriodsSet) =>
        submit(noPeriodsSetForm, ReturnPeriodsChoicePage, mode)(errors => noReturnPeriodsSetView(errors, mode))

      case Some(Standard(returnPeriodsId)) =>
        submit(standardForm, StandardReturnPeriodsPage, mode)(errors => standardReturnPeriodsView(errors, mode, returnPeriodsId))

      case Some(NonStandard(viewModel)) =>
        submit(nonStandardLastForm, NonStandardReturnPeriodsPage, mode)(errors => nonStandardReturnPeriodsView(errors, viewModel, mode))

      case Some(NonStandardNotLast(viewModel)) =>
        submit(nonStandardNotLastForm, NonStandardNotLastReturnPeriodsYesNoPage, mode)(errors =>
          nonStandardReturnPeriodsView(errors, viewModel, mode)
        )

      case Some(LockedPeriods) =>
        Future.successful(Redirect(controllers.routes.ChangeRegistrationDetailsController.onPageLoad()))

      case None =>
        Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))

    }
  }

  private def screenFor(userAnswers: UserAnswers)(implicit messages: Messages): Option[ReturnPeriodsScreen] =
    userAnswers.get(GamblingReturnPeriodsPage).flatMap { periods =>
      if periods.returnPeriodsId.isEmpty then Some(NoPeriodsSet)
      else if (userAnswers.get(HasSwitchedToStandardValuesPage).contains(true)) Some(LockedPeriods)
      else
        periods.hasExistingNstpValues.flatMap {
          case false => Some(Standard(periods.returnPeriodsId.map(_.toString)))
          case true =>
            periods.isInLastNstp.flatMap {
              case true =>
                NonStandardReturnPeriodsViewModel.from(periods).map(NonStandard(_))
              case false =>
                NonStandardReturnPeriodsViewModel.from(periods).map(NonStandardNotLast(_))
            }

        }
    }

  private def prepare[A: Reads](form: Form[A], page: QuestionPage[A])(implicit request: DataRequest[?]): Form[A] =
    request.userAnswers.get(page).fold(form)(form.fill)

  private def submit[A: Writes](form: Form[A], page: QuestionPage[A], mode: Mode)(
    errorView: Form[A] => HtmlFormat.Appendable
  )(implicit request: DataRequest[AnyContent]): Future[Result] = form
    .bindFromRequest()
    .fold(
      formWithErrors => Future.successful(BadRequest(errorView(formWithErrors))),
      value =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
          _              <- sessionRepository.set(updatedAnswers)
        } yield Redirect(navigator.nextPage(page, mode, updatedAnswers))
    )
}

object ReturnPeriodsController {
  private sealed trait ReturnPeriodsScreen
  private case object NoPeriodsSet                                                          extends ReturnPeriodsScreen
  private case object LockedPeriods                                                         extends ReturnPeriodsScreen
  private final case class Standard(returnPeriodsId: Option[String])                        extends ReturnPeriodsScreen
  private final case class NonStandard(viewModel: NonStandardReturnPeriodsViewModel)        extends ReturnPeriodsScreen
  private final case class NonStandardNotLast(viewModel: NonStandardReturnPeriodsViewModel) extends ReturnPeriodsScreen
}
