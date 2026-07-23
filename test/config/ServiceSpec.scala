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

package config

import base.SpecBase
import com.typesafe.config.ConfigFactory
import play.api.Configuration

class ServiceSpec extends SpecBase {

  "Service" - {

    "must build the correct baseUrl" in {

      val service = Service(
        host     = "localhost",
        port     = "8080",
        protocol = "http"
      )

      service.baseUrl mustEqual "http://localhost:8080"
    }

    "must return the baseUrl from toString" in {

      val service = Service(
        host     = "example.com",
        port     = "443",
        protocol = "https"
      )

      service.toString mustEqual "https://example.com:443"
    }

    "must implicitly convert to a String" in {

      val service = Service(
        host     = "localhost",
        port     = "9000",
        protocol = "http"
      )

      val url: String = service

      url mustEqual "http://localhost:9000"
    }

    "must load a Service from configuration" in {

      val configuration = Configuration(
        ConfigFactory.parseString(
          """
            |service {
            |  host = "localhost"
            |  port = "8080"
            |  protocol = "http"
            |}
            |""".stripMargin
        )
      )

      configuration.get[Service]("service") mustEqual Service(
        host     = "localhost",
        port     = "8080",
        protocol = "http"
      )
    }
  }
}
