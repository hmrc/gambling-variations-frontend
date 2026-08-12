package viewmodels.checkAnswers.businessaddress

import base.SpecBase
import models.BusinessAddressChangeAddrOption.*
import models.{Address, UserAnswers}
import pages.*
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class HowToChangeBusinessAddressRowSpec extends SpecBase {

  trait Setup {
    val app: Application = applicationBuilder().build()
    implicit val request: play.api.mvc.Request[?] = FakeRequest()
    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

  }

  "HowToChangeBusinessAddressRow" - {

    "must show correct data for 'different uk address' option" in new Setup {
      private val addressUa: Address = Address(
        "address1",
        Some("address2"),
        Some("address3"),
        Some("address4"),
        Some("postcode"),
        Some("country"))


      private val ua = emptyUserAnswers.
        set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessAddressChangeScreenerPage, DifferentUkAddress)
        .success
        .value

      private val result: SummaryListRow = HowToChangeBusinessAddressRow.from(ua)

      result.key.content mustEqual Text(messages("checkBusinessAddress.question.howToChange"))
      result.value.content mustEqual Text(messages("checkBusinessAddress.changeOption.difUk"))
    }

    "must show correct data for 'change to non uk address' option" in new Setup {
      private val addressUa: Address = Address(
        "address1",
        Some("address2"),
        Some("address3"),
        Some("address4"),
        Some("postcode"),
        Some("country"))


      private val ua = emptyUserAnswers.
        set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessAddressChangeScreenerPage, ChangeToNonUkAddress)
        .success
        .value

      private val result: SummaryListRow = HowToChangeBusinessAddressRow.from(ua)

      result.key.content mustEqual Text(messages("checkBusinessAddress.question.howToChange"))
      result.value.content mustEqual Text(messages("checkBusinessAddress.changeOption.nonUk"))
    }

    "must show correct data for 'change to uk address' option" in new Setup {
        private val addressUa: Address = Address(
          "address1",
          Some("address2"),
          Some("address3"),
          Some("address4"),
          Some("postcode"),
          Some("country"))


        private val ua: UserAnswers = emptyUserAnswers.
          set(BusinessAddressUkPage, addressUa)
          .success
          .value
          .set(BusinessAddressChangeScreenerPage, ChangeToUkAddress)
          .success
          .value

        private val result: SummaryListRow = HowToChangeBusinessAddressRow.from(ua)

        result.key.content mustEqual Text(messages("checkBusinessAddress.question.howToChange"))
        result.value.content mustEqual Text(messages("checkBusinessAddress.changeOption.toUk"))
      }

    "must show correct data for 'edit current address' option" in new Setup {
      private val addressUa: Address = Address(
        "address1",
        Some("address2"),
        Some("address3"),
        Some("address4"),
        Some("postcode"),
        Some("country"))


      private val ua: UserAnswers = emptyUserAnswers.
        set(BusinessAddressUkPage, addressUa)
        .success
        .value
        .set(BusinessAddressChangeScreenerPage, EditCurrentAddress)
        .success
        .value

      private val result: SummaryListRow = HowToChangeBusinessAddressRow.from(ua)

      result.key.content mustEqual Text(messages("checkBusinessAddress.question.howToChange"))
      result.value.content mustEqual Text(messages("checkBusinessAddress.changeOption.edit"))
    }
  }
}
