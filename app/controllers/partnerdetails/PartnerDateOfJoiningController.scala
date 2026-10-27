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

import connectors.GamblingConnector
import controllers.actions.*
import forms.partnerdetails.PartnerDateOfJoiningFormProvider
import models.Mode
import navigation.Navigator
import pages.partnerdetails.PartnerDetailsDateOfJoiningPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateTimeFormats.*
import utils.PartnerUtils
import views.html.partnerdetails.PartnerDateOfJoiningView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerDateOfJoiningController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  formProvider: PartnerDateOfJoiningFormProvider,
  gamblingConnector: GamblingConnector,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDateOfJoiningView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(index: String, mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      implicit val lang: Lang = messagesApi.preferred(request).lang
      val page = PartnerDetailsDateOfJoiningPage(PartnerUtils.parseIndex(index, mode))

      loadPageContext(request.mgdRegNum).map { ctx =>
        val preparedForm = request.userAnswers.get(page).fold(ctx.form)(ctx.form.fill)
        Ok(view(preparedForm, index, mode, ctx.registrationDate, ctx.latestDate))
      }
    }

  def onSubmit(index: String, mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      implicit val lang: Lang = messagesApi.preferred(request).lang
      val page = PartnerDetailsDateOfJoiningPage(PartnerUtils.parseIndex(index, mode))

      loadPageContext(request.mgdRegNum).flatMap { ctx =>
        ctx.form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, index, mode, ctx.registrationDate, ctx.latestDate))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(page, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(page, mode, updatedAnswers))
          )
      }
    }

  private final case class PageContext(form: Form[LocalDate], registrationDate: String, latestDate: String)

  private def loadPageContext(mgdRegNum: String)(implicit messages: Messages, hc: HeaderCarrier, lang: Lang): Future[PageContext] =
    gamblingConnector.getBusinessDetails(mgdRegNum).flatMap { businessDetails =>
      businessDetails.dateOfRegistration match {
        case Some(registrationDate) =>
          Future.successful(
            PageContext(
              form             = formProvider(registrationDate),
              registrationDate = registrationDate.format(dateTimeFormat()),
              latestDate       = latestDate(registrationDate).format(dateTimeHintFormat)
            )
          )
        case None =>
          Future.failed(new RuntimeException("No registration date found"))
      }
    }

  private def latestDate(registrationDate: LocalDate): LocalDate = {
    val today = LocalDate.now()
    (if (registrationDate.isAfter(today)) registrationDate else today).plusDays(15)
  }
}
