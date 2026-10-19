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

package models.controllingbody

import models.{Enumerable, WithName}

sealed trait ControllingBodyChangeOption

object ControllingBodyChangeOption extends Enumerable.Implicits {

  case object EditDetails extends WithName("editDetails") with ControllingBodyChangeOption
  case object ProvideNew  extends WithName("provideNew") with ControllingBodyChangeOption
  case object KeepSame    extends WithName("keepSame") with ControllingBodyChangeOption

  val values: Seq[ControllingBodyChangeOption] = Seq(
    EditDetails,
    ProvideNew,
    KeepSame
  )

  implicit val enumerable: Enumerable[ControllingBodyChangeOption] =
    Enumerable(values.map(v => v.toString -> v)*)
}
