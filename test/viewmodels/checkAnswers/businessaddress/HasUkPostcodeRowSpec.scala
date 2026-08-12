package viewmodels.checkAnswers.businessaddress

import base.SpecBase
import models.Address
import pages.*
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class HasUkPostcodeRowSpec extends SpecBase {

  trait Setup {
    val app: Application = applicationBuilder().build()
    implicit val request: play.api.mvc.Request[?] = FakeRequest()
    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

  }

  "HasUkPostcodeRow" - {

    "must render summary list row with correct data" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))

      private val ua = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessAddressHasUkPostcodePage, true)
        .success
        .value

      private val result: SummaryListRow = HasUkPostcodeRow.from(ua)

      result.key.content mustEqual Text(messages("checkBusinessAddress.question.ukPostcode"))
      result.value.content mustEqual Text(messages("site.yes"))
    }
  }
}
