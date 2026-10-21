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

package base

import com.typesafe.config.ConfigFactory
import controllers.actions.*
import models.UserAnswers
import config.FrontendAppConfig
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import play.api.{Application, Configuration}
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.Future

trait SpecBase extends AnyFreeSpec with Matchers with TryValues with OptionValues with ScalaFutures with IntegrationPatience with MockFactory {

  implicit val actorSystem: ActorSystem = ActorSystem("unit-tests")
  implicit val mat: Materializer = Materializer.createMaterializer(actorSystem)

  val userAnswersId: String = "id"
  val mgdRegNum: String = "XRM00000000574"

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  private def defaultSessionRepository: SessionRepository = {
    val repository = Mockito.mock(classOf[SessionRepository])
    Mockito.when(repository.get(ArgumentMatchers.any())).thenReturn(Future.successful(None))
    Mockito.when(repository.set(ArgumentMatchers.any())).thenReturn(Future.successful(true))
    Mockito.when(repository.clear(ArgumentMatchers.any())).thenReturn(Future.successful(true))
    Mockito.when(repository.keepAlive(ArgumentMatchers.any())).thenReturn(Future.successful(true))
    repository
  }

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      // The data required actions are eager singletons depending on SessionRepository, so without
      // this default every application would need a running Mongo. Specs may still override it.
      .bindings(bind[SessionRepository].toInstance(defaultSessionRepository))
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[BusinessNameDataRequiredAction].to[BusinessNameDataRequiredActionImpl],
        bind[BusinessContactDetailsDataRequiredAction].to[BusinessContactDetailsDataRequiredActionImpl],
        bind[MgdTradeDetailsDataRequiredAction].to[MgdTradeDetailsDataRequiredActionImpl],
        bind[BusinessDetailsDataRequiredAction].to[BusinessDetailsDataRequiredActionImpl],
        bind[CorrespondenceDetailsDataRequiredAction].to[CorrespondenceDetailsDataRequiredActionImpl],
        bind[BusinessAddressDataRequiredAction].to[BusinessAddressDataRequiredActionImpl],
        bind[PartnerDetailsDataRequiredAction].to[PartnerDetailsDataRequiredActionImpl],
        bind[AuthorisedAction].to[FakeAuthorisedAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )

  private val testConfiguration: Configuration =
    Configuration(
      ConfigFactory.parseString(
        """
          |host = "http://localhost:9000"
          |
          |mongodb {
          |  timeToLiveInSeconds = 900
          |}
          |
          |urls {
          |  login = "http://gambling-variations/login"
          |  loginContinue = "http://gambling-variations/bar"
          |  signOut = "http://gambling-variations/sign-out"
          |  hmrcOnlineServiceDesk = "http://gambling-variations/hmrc-online-service-desk"
          |  gamblingManagementHome = "http://gambling-variations/home"
          |  account = "http://gambling-variations/account"
          |}
          |
          |timeout-dialog {
          |  timeout   = 10
          |  countdown = 5
          |}
          |
          |contact-frontend {
          |  host = "http://localhost:9250"
          |}
          |
          |address-lookup {
          |  home-nav-href = "http://gambling-variations/home"
          |  deskpro-service-name = "gambling-variations-frontend"
          |  timeout-amount = 900
          |  timeout-url = "http://localhost:9000/gambling-variations/there-is-a-problem"
          |  timeout-keep-alive-url = "http://localhost:9000/gambling-variations/refresh-session"
          |}
          |
          |microservice {
          |  services {
          |    auth {
          |      protocol = "http"
          |      host     = "localhost"
          |      port     = 8500
          |    }
          |
          |    feedback-frontend {
          |      protocol = "http"
          |      host     = "localhost"
          |      port     = 9514
          |    }
          |
          |    address-lookup-frontend {
          |      protocol = "http"
          |      host     = "localhost"
          |      port     = 9028
          |    }
          |
          |    accessibility-statement {
          |      protocol = "http"
          |      host     = "localhost"
          |      port     = 12346
          |    }
          |  }
          |}
          |
          |features {
          |  welsh-translation = false
          |}
          |""".stripMargin
      )
    )

  protected val testFrontendAppConfig: FrontendAppConfig =
    new FrontendAppConfig(testConfiguration)

}
