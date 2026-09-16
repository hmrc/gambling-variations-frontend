package controllers

import controllers.actions._
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PremisesAddressListView

class PremisesAddressListController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       authorise: AuthorisedAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: PremisesAddressListView
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authorise andThen getData andThen requireData) {
    implicit request =>
      Ok(view())
  }
}
