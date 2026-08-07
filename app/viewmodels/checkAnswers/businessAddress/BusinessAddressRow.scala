package viewmodels.checkAnswers.businessAddress

import controllers.routes
import models.{Address, UserAnswers}
import pages.{BusinessAddressNonUkPage, BusinessAddressUkPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryListRow, Value}
import viewmodels.govuk.all.{ActionItemViewModel, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*


class BusinessAddressRow {
  def from(address: Option[Address], isUk: Boolean)(implicit messages: Messages): SummaryListRow = {

    val addressRowValue: Value = {
      address match {
        case Some(addr) => ValueViewModel(
          s"""
                         ${addr.address1}
                         ${addr.address2.getOrElse(messages("site.notProvided"))}
                         ${addr.address2.getOrElse(messages("site.notProvided"))}
                         ${addr.address3.getOrElse(messages("site.notProvided"))}
                         ${addr.address4.getOrElse(messages("site.notProvided"))}
                         ${if(isUk) addr.postcode.getOrElse("site.notProvided")}
                         ${if(!isUk) addr.country.getOrElse("site.notProvided")}
                         """
        )
        case None => ValueViewModel("site.notProvided")
      }

    }

    SummaryListRowViewModel(
      key = "checkBusinessAddress.label.address",
      value = addressRowValue,
      actions = Seq(ActionItemViewModel("site.change", routes.PageNotFoundController.onPageLoad().url))
    )
  }
}



