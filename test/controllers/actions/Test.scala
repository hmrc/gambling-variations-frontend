package controllers.actions

import base.SpecBase
import connectors.GamblingConnector
import controllers.Execution.trampoline
import models.requests.{DataRequest, OptionalDataRequest}
import models.{PartnerDetails, PartnersDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.FakeRequest
import repositories.SessionRepository

import java.time.LocalDate
import scala.concurrent.Future

class Test extends SpecBase with MockitoSugar {

  val gamblingConnector: GamblingConnector = mock[GamblingConnector]
  val sessionRepository: SessionRepository = mock[SessionRepository]

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends PartnerDetailsDataRequiredActionImpl(sessionRepository, gamblingConnector) {

    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = this.refine(request)
  }

  val partnerDetails = PartnersDetails(
    partners = Seq(
      PartnerDetails(
        mgdRegNumber           = "reg1",
        dateOfJoining          = Some(LocalDate.of(2026, 10, 1)),
        dateOfLeaving          = Some(LocalDate.of(2026, 12, 1)),
        solePropTitle          = Some("solePropTitle1"),
        solePropFirstName      = Some("solePropFirstName1"),
        solePropMiddleName     = Some("solePropMiddleName1"),
        solePropLastName       = Some("solePropLastName1"),
        businessName           = Some("BusinessName1"),
        tradingName            = Some("tradingName1"),
        dateOfBirth            = Some(LocalDate.of(1900, 1, 1)),
        nino                   = Some("nino1"),
        utr                    = Some("utr1"),
        vrn                    = Some("vrn1"),
        crn                    = Some("crn1"),
        dateOfIncorporation    = Some(LocalDate.of(2000, 12, 1)),
        countryOfIncorporation = Some("countryOfIncorportation1"),
        foreignCorporateRef    = Some("foreignCorporateRef1"),
        address1               = Some("address1-1"),
        address2               = Some("address2-1"),
        address3               = Some("address3-1"),
        address4               = Some("address4-1"),
        postcode               = Some("postcode1"),
        country                = Some("country1"),
        adi                    = Some("adi1"),
        iomOrCiFlag            = Some("iomOrCiFlag1"),
        phoneNumber            = Some("phoneNumber1"),
        mobilePhoneNumber      = Some("mobilePhoneNumber1"),
        faxNumber              = Some("faxNumber1"),
        emailAddress           = Some("emailAddress1"),
        isFutureLeaveDate      = Some(1),
        isFutureJoinDate       = Some(1),
        businessType           = Some(1)
      ),
      PartnerDetails(
        mgdRegNumber           = "reg2",
        dateOfJoining          = Some(LocalDate.of(2026, 10, 2)),
        dateOfLeaving          = Some(LocalDate.of(2026, 12, 2)),
        solePropTitle          = Some("solePropTitle2"),
        solePropFirstName      = Some("solePropFirstName2"),
        solePropMiddleName     = Some("solePropMiddleName2"),
        solePropLastName       = Some("solePropLastName2"),
        businessName           = Some("BusinessName2"),
        tradingName            = Some("tradingName2"),
        dateOfBirth            = Some(LocalDate.of(1900, 1, 2)),
        nino                   = Some("nino2"),
        utr                    = Some("utr2"),
        vrn                    = Some("vrn2"),
        crn                    = Some("crn2"),
        dateOfIncorporation    = Some(LocalDate.of(2000, 12, 2)),
        countryOfIncorporation = Some("countryOfIncorportation2"),
        foreignCorporateRef    = Some("foreignCorporateRef2"),
        address1               = Some("address1-2"),
        address2               = Some("address2-2"),
        address3               = Some("address3-2"),
        address4               = Some("address4-2"),
        postcode               = Some("postcode2"),
        country                = Some("country2"),
        adi                    = Some("adi2"),
        iomOrCiFlag            = Some("iomOrCiFlag2"),
        phoneNumber            = Some("phoneNumber2"),
        mobilePhoneNumber      = Some("mobilePhoneNumber2"),
        faxNumber              = Some("faxNumber2"),
        emailAddress           = Some("emailAddress2"),
        isFutureLeaveDate      = Some(2),
        isFutureJoinDate       = Some(2),
        businessType           = Some(2)
      )
    ),
    systemDate = None
  )

  "What will it print:" in {

    val request = FakeRequest()

    when(gamblingConnector.getPartnerDetails(any())(any())) thenReturn Future.successful(partnerDetails)
    when(sessionRepository.set(any())).thenReturn(Future.successful(true))

    val action = new Harness(sessionRepository, gamblingConnector)

    val z = action.callRefine(OptionalDataRequest(request, "XRM00000000574", None)).futureValue

    println(z.map(e => e.userAnswers))
  }

}
