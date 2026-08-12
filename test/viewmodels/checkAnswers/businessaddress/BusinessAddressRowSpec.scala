package viewmodels.checkAnswers.businessaddress

import base.SpecBase
import models.{Address, NormalMode}
import pages.*
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class BusinessAddressRowSpec extends SpecBase {

  trait Setup {
    val app: Application = applicationBuilder().build()
    implicit val request: play.api.mvc.Request[?] = FakeRequest()
    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

  }

  "BusinessAddressRow" - {

    "must render uk address without country and with postcode" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))

      private val ua = emptyUserAnswers
        .set(BusinessAddressUkPage, addressUa)
        .success
        .value

      private val result: SummaryListRow = BusinessAddressRow(ua, NormalMode).toRow

      result.key.content mustEqual Text(messages("checkBusinessAddress.label.address"))
      result.value.content mustEqual Text(
        s"""|address1
           |address2
           |address3
           |address4
           |postcode""".stripMargin
      )
    }

    "must render non uk address without postcode and with country" in new Setup {
      private val addressUa: Address = Address("address1", Some("address2"), Some("address3"), Some("address4"), Some("postcode"), Some("country"))

      private val ua = emptyUserAnswers
        .set(BusinessAddressNonUkPage, addressUa)
        .success
        .value

      private val result: SummaryListRow = BusinessAddressRow(ua, NormalMode).toRow

      result.key.content mustEqual Text(messages("checkBusinessAddress.label.address"))
      result.value.content mustEqual Text(
        s"""|address1
           |address2
           |address3
           |address4
           |country""".stripMargin
      )
    }
  }
}
