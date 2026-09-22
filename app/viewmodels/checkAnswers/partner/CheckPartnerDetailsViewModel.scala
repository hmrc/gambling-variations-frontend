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

package viewmodels.checkAnswers.partner

import models.{Address, ContactNumber, UserAnswers}
import pages.partner.*
import pages.partnerdetails.*
import play.api.i18n.Messages

case class CheckPartnerDetailsViewModel(
  //  Business Details
  typeOfBusiness: Option[String],
  soleProprietorName: Option[String],
  soleProprietorDob: Option[String],
  unincorporatedBodyName: Option[String],
  partnershipName: Option[String],
  llpName: Option[String],
  addTradingName: Option[String],
  tradingName: Option[String],
  dateOfJoining: Option[String],
  addNino: Option[String],
  nino: Option[String],
  utr: Option[String],
  addVatRegistrationNumber: Option[String],
  vatRegistrationNumber: Option[String],
  isIncorporatedInUk: Option[String],
  countryOfIncorporation: Option[String],
  dateOfIncorporation: Option[String],
  foreignCorporateReference: Option[String],
  companyRegistrationNumber: Option[String],

  //  Address
  address: Address,
  addAdditionalInformation: Option[String],
  additionalInformation: Option[String],

  //  Contact Details
  contactNumbers: Option[ContactNumber],
  addFaxNumber: Option[Boolean],
  faxNumber: Option[String],
  addEmailAddress: Option[Boolean],
  emailAddress: Option[String]
)

object CheckPartnerDetailsViewModel {

  def from(userAnswers: UserAnswers, index: Int)(implicit messages: Messages): CheckPartnerDetailsViewModel = {
    CheckPartnerDetailsViewModel(
      // Business Details
      typeOfBusiness            = userAnswers.get(PartnerDetailsBusinessTypePage(index)).map(_.toString),
      soleProprietorName        = userAnswers.get(PartnerDetailsSoleProprietorPage(index)).map(_.fullName),
      soleProprietorDob         = userAnswers.get(PartnerDetailsDateOfBirthPage(index)).map(_.toString),
      unincorporatedBodyName    = userAnswers.get(PartnerDetailsBusinessNamePage(index)),
      partnershipName           = userAnswers.get(PartnerDetailsBusinessNamePage(index)),
      llpName                   = userAnswers.get(PartnerDetailsBusinessNamePage(index)),
      addTradingName            = userAnswers.get(PartnerDetailsAddTradingNameYesNoPage(index)).map(_.toString),
      tradingName               = userAnswers.get(PartnerTradingNamePage).orElse(userAnswers.get(PartnerDetailsTradingNamePage(index))),
      dateOfJoining             = userAnswers.get(PartnerDetailsDateOfJoiningPage(index)).map(_.toString),
      addNino                   = userAnswers.get(PartnerDetailsAddNationalInsuranceNumberYesNoPage(index)).map(_.toString),
      nino                      = userAnswers.get(PartnerDetailsNinoPage(index)),
      utr                       = userAnswers.get(PartnerDetailsUtrPage(index)),
      addVatRegistrationNumber  = userAnswers.get(VatRegistrationNumberYesNoPage(index)).map(_.toString),
      vatRegistrationNumber     = userAnswers.get(PartnerDetailsVrnPage(index)),
      isIncorporatedInUk        = userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(index)).map(_.toString),
      countryOfIncorporation    = userAnswers.get(PartnerDetailsCountryOfIncorporation(index)),
      dateOfIncorporation       = userAnswers.get(PartnerDetailsDateOfIncorporation(index)).map(_.toString),
      foreignCorporateReference = userAnswers.get(PartnerDetailsForeignCorporateReferencePage(index)),
      companyRegistrationNumber = userAnswers.get(PartnerDetailsCrnPage(index)),

      // Address
      // Temporary stub until Address page lookup is wired up,
      address = Address(
        address1 = "address1",
        address2 = Some("address2"),
        address3 = Some("address3"),
        address4 = Some("address4"),
        postcode = Some("postcode"),
        country  = Some("country")
      ),
      addAdditionalInformation = userAnswers.get(PartnerDetailsAdditionalAddressInfoYesNoPage).map(_.toString),
      additionalInformation    = userAnswers.get(PartnerDetailsAdditionalAddressInfoPage),

      // Contact Details
      contactNumbers  = userAnswers.get(PartnerDetailsContactNumberPage(index)),
      addFaxNumber    = userAnswers.get(PartnerAddFaxNumberYesNoPage(index)),
      faxNumber       = userAnswers.get(PartnerDetailsCorrespondenceFaxNumberPage(index)),
      addEmailAddress = userAnswers.get(PartnerAddEmailAddressYesNoPage(index)),
      emailAddress    = userAnswers.get(PartnerEmailAddressPage).orElse(userAnswers.get(PartnerDetailsCorrespondenceEmailAddressPage(index)))
    )
  }
}
