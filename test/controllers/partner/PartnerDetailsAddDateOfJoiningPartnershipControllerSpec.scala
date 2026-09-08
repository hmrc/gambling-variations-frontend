package controllers.partner

import base.SpecBase
import forms.partner.PartnerDetailsAddDateOfJoiningPartnershipFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.PartnerDetailsAddDateOfJoiningPartnershipPage
import play.api.i18n.{Lang, Messages}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import utils.DateTimeFormats.dateTimeFormat
import views.html.partner.PartnerDetailsAddDateOfJoiningPartnershipView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

//TODO think if I need more tests here
class PartnerDetailsAddDateOfJoiningPartnershipControllerSpec extends SpecBase with MockitoSugar {

  private implicit val messages: Messages = stubMessages()

  private val formProvider = new PartnerDetailsAddDateOfJoiningPartnershipFormProvider()
  private val dateNow = LocalDate.now() // TODO I can't use date now, it will break tests
  private val twoWeeksLater = dateNow.plusDays(14)
  private val formatter = dateTimeFormat()(Lang("en"))

  private def form = formProvider(twoWeeksLater) // TODO

  def onwardRoute = Call("GET", "/foo")

  val validAnswer = LocalDate.now(ZoneOffset.UTC)

  lazy val partnerDetailsAddDateOfJoiningPartnershipRoute = routes.PartnerDetailsAddDateOfJoiningPartnershipController.onPageLoad().url

  override val emptyUserAnswers = UserAnswers(userAnswersId)

  // TODO rename
  val jsObject = Json.obj(
    "dateOfRegistration" -> "2026-09-07",
    "isGroupMember"      -> false,
    "businessType"       -> 1,
    "partners" -> Json.arr(
      Json.obj(
        "partnerDetailsMgdRegNumber" -> "XWM00000001762"
      )
    )
  )
  val minimalUserAnswers = UserAnswers(userAnswersId, jsObject)

  // TODO rename
  // TODO check which field we actually modify!
  val jsObject2 = Json.obj(
    "dateOfRegistration" -> "2026-09-07",
    "isGroupMember"      -> false,
    "businessType"       -> 1,
    "partners" -> Json.arr(
      Json.obj(
        "partnerDetailsMgdRegNumber"  -> "XWM00000001762",
        "partnerDetailsDateOfJoining" -> "2026-09-07" // TODO this might be changed to dateOfJoining
      )
    )
  )
  val userAnswers = UserAnswers(userAnswersId, jsObject2)

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, partnerDetailsAddDateOfJoiningPartnershipRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, partnerDetailsAddDateOfJoiningPartnershipRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "PartnerDetailsAddDateOfJoiningPartnership Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(minimalUserAnswers)).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[PartnerDetailsAddDateOfJoiningPartnershipView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, twoWeeksLater.format(formatter))(getRequest(), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[PartnerDetailsAddDateOfJoiningPartnershipView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode, twoWeeksLater.format(formatter))(
          getRequest(),
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(minimalUserAnswers)).build()

      val request =
        FakeRequest(POST, partnerDetailsAddDateOfJoiningPartnershipRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PartnerDetailsAddDateOfJoiningPartnershipView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, twoWeeksLater.format(formatter))(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to SystemError for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
