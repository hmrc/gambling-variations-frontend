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

package models

sealed trait CorrespondenceChangeAddrOption

object CorrespondenceChangeAddrOption extends Enumerable.Implicits {

  case object DifferentUkAddress   extends WithName("differentUkAddress") with CorrespondenceChangeAddrOption
  case object ChangeToNonUkAddress extends WithName("nonUkAddress") with CorrespondenceChangeAddrOption
  case object ChangeToUkAddress    extends WithName("ukAddress") with CorrespondenceChangeAddrOption
  case object EditCurrentAddress   extends WithName("editCurrentAddress") with CorrespondenceChangeAddrOption

  val values: Seq[CorrespondenceChangeAddrOption] = Seq(
    DifferentUkAddress,
    ChangeToNonUkAddress,
    ChangeToUkAddress,
    EditCurrentAddress
  )

  implicit val enumerable: Enumerable[CorrespondenceChangeAddrOption] =
    Enumerable(values.map(v => v.toString -> v)*)
}
