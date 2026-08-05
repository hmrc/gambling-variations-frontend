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

import com.ibm.icu.util.LocaleData
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

////TODO it should re-use a lot of schema already existing
//case class NewPartnerDetails(
//  //  mgdRegNumber: String,
//  entityName: EntityName,
//  // EntityName can be either SoleProprietorNameDetails:
//  //  mgdRegNum: String,
//  //  title: String,
//  //  firstName: String,
//  //  middleName: Option[String],
//  //  lastName: String,
//  //  tradingName: Option[String],
//  //  businessType: BusinessType,
//  //  systemDate: Option[LocalDate]
//  // or BusinessNameDetails:
//  //  mgdRegNum: String,
//  //  businessName: String,
//  //  businessType: BusinessType,
//  //  tradingName: Option[String],
//  //  systemDate: Option[LocalDate]
//
//  correspondenceDetails: CorrespondenceDetails,
//  // Comes with:
//  // mgdRegNumber: String,
//  // nameLine1: Option[String],
//  // nameLine2: Option[String],
//  // correspondenceAddress: Option[Address],
//  // additionalInformation: Option[String],
//  // iomOrCiFlag: Option[String],
//  // contactNumber: Option[ContactNumber],
//  // faxNumber: Option[String],
//  // emailAddr: Option[String]
//
//  // TODO these two are inside of correspondeDetails
//  contactNumber: ContactNumber,
//  // phoneNumber: Option[String],
//  // mobilePhoneNumber: Option[String]
//
////  address: Address,
//  // address1: String,
//  // address2: Option[String],
//  // address3: Option[String],
//  // address4: Option[String],
//  // postcode: Option[String],
//  // country: Option[String]
//
//  // TODO this one looks quite promising
//  partnerMember: PartnerMember
//)
//
////TODO page suffix?
//case class PartnerDetails(
//  mgdRegNumber: String,
//  businessEmail: Option[String],
//  faxNumber: Option[String],
//  mobileNumber: Option[String],
//  phoneNumber: Option[String]
//)

////////////////////////////////////////////////////////////////////////
//TODO this is an actual structure backend will provide from rds
final case class PartnersDetails(
  partners: Seq[PartnerDetails],
  systemDate: Option[LocalDate]
)

final case class PartnerDetails(
  mgdRegNumber: String,
  dateOfJoining: Option[LocalDate],
  dateOfLeaving: Option[LocalDate],
  soleProprietorNameDetails: Option[SoleProprietorNameDetails],
  solePropTitle: Option[String],
  solePropFirstName: Option[String],
  solePropMiddleName: Option[String],
  solePropLastName: Option[String],
  businessName: Option[String],
  tradingName: Option[String],
  dateOfBirth: Option[LocalDate],
  nino: Option[String],
  utr: Option[String],
  vrn: Option[String],
  crn: Option[String],
  dateOfIncorporation: Option[LocalDate],
  countryOfIncorporation: Option[String],
  foreignCorporateRef: Option[String],
  address1: Option[String],
  address2: Option[String],
  address3: Option[String],
  address4: Option[String],
  postcode: Option[String],
  country: Option[String],
  adi: Option[String],
  iomOrCiFlag: Option[String],
  phoneNumber: Option[String],
  mobilePhoneNumber: Option[String],
  faxNumber: Option[String],
  emailAddress: Option[String],
  isFutureLeaveDate: Option[Int],
  isFutureJoinDate: Option[Int],
  businessType: Option[Int]
)
object PartnerDetails {

  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val format: OFormat[PartnerDetails] =
    Json.format[PartnerDetails]
}

object PartnersDetails {
  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val format: OFormat[PartnersDetails] =
    Json.format[PartnersDetails]
}

////////////////////////////////////////////////////////////////////////

//TODO wip from RDS
//RAw because that's what I get from backend -> RDS, but here it should be a little "processed"
//final case class PartnerRawData(
//  mgdRegNumber: String, // TODO ✅
//  dateOfJoining: Option[LocalDate],
//  dateOfLeaving: Option[LocalDate],
//  solePropTitle: Option[String], // TODO ✅
//  solePropFirstName: Option[String], // TODO ✅
//  solePropMiddleName: Option[String], // TODO ✅
//  solePropLastName: Option[String], // TODO ✅
//  businessName: Option[String], // TODO ✅
//  tradingName: Option[String], // TODO ✅
//  dateOfBirth: Option[LocalDate],
//  nino: Option[String],
//  utr: Option[String],
//  vrn: Option[String],
//  crn: Option[String],
//  dateOfIncorporation: Option[LocalDate],
//  countryOfIncorporation: Option[String],
//  foreignCorporateRef: Option[String],
//  address1: Option[String], // TODO ✅
//  address2: Option[String], // TODO ✅
//  address3: Option[String], // TODO ✅
//  address4: Option[String], // TODO ✅
//  postcode: Option[String], // TODO ✅
//  country: Option[String], // TODO ✅
//  adi: Option[String],
//  iomOrCiFlag: Option[String], // TODO ✅
//  phoneNumber: Option[String], // TODO Done
//  mobilePhoneNumber: Option[String], // TODO Done
//  faxNumber: Option[String], // TODO Done
//  emailAddress: Option[String], // TODO Done
//
//  isFutureLeaveDate: Option[Int],
//  isFutureJoinDate: Option[Int],
//  businessType: Option[Int], // TODO ✅
//  systemDate: Option[LocalDate] // TODO technically this would be outside of this array
//) {
//
//  def toConcreteClass: NewPartnerDetails = NewPartnerDetails(
//    entityName =
//      if businessType.getOrElse(0) == 1 then
//        SoleProprietorNameDetails(
//          mgdRegNum    = mgdRegNumber,
//          title        = solePropTitle.getOrElse(""),
//          firstName    = solePropFirstName.getOrElse(""),
//          middleName   = solePropMiddleName,
//          lastName     = solePropLastName.getOrElse(""),
//          tradingName  = tradingName,
//          businessType = businessType.flatMap(BusinessType.fromCode).get,
//          systemDate   = systemDate
//        )
//      else
//        BusinessNameDetails(
//          mgdRegNum    = mgdRegNumber,
//          businessName = businessName.getOrElse(""),
//          businessType = businessType.flatMap(BusinessType.fromCode).get,
//          tradingName  = tradingName,
//          systemDate   = systemDate // TODO this will come from other place
//        )
//    ,
//    correspondenceDetails = CorrespondenceDetails(
//      mgdRegNumber = mgdRegNumber,
//      nameLine1    = businessName,
//      nameLine2    = businessName, // TODO
//      correspondenceAddress = Some(
//        Address(
//          address1 = address1.get, // TODO
//          address2 = address2,
//          address3 = address3,
//          address4 = address4,
//          postcode = postcode,
//          country  = country
//        )
//      ),
//      additionalInformation = ???,
//      iomOrCiFlag           = iomOrCiFlag,
//      contactNumber = Some(
//        ContactNumber(
//          phoneNumber       = phoneNumber,
//          mobilePhoneNumber = mobilePhoneNumber
//        )
//      ),
//      faxNumber = faxNumber,
//      emailAddr = emailAddress
//    ),
//    contactNumber = ContactNumber(
//      phoneNumber       = phoneNumber,
//      mobilePhoneNumber = mobilePhoneNumber
//    ),
////    address = ???,
//    partnerMember = PartnerMember(
//      namesOfPartMems    = ???,
//      solePropTitle      = solePropTitle,
//      solePropFirstName  = solePropFirstName,
//      solePropMiddleName = solePropMiddleName,
//      solePropLastName   = solePropLastName,
//      typeOfBusiness     = businessType.flatMap(BusinessType.fromCode).get
//    )
//  )
//
//}
//
//object PartnerRawData {
//
//  implicit val writes: OWrites[PartnerRawData] = Json.writes[PartnerRawData]
//
//  implicit val reads: Reads[PartnerRawData] = (
//    (__ \ "mgdRegNumber").read[String] and
//      (__ \ "dateOfJoining").readNullable[LocalDate] and
//      (__ \ "dateOfLeaving").readNullable[LocalDate] and
//      (__ \ "solePropTitle").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "solePropFirstName").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "solePropMiddleName").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "solePropLastName").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "businessName").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "tradingName").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "dateOfBirth").readNullable[LocalDate] and
//      (__ \ "nino").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "utr").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "vrn").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "crn").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "dateOfIncorporation").readNullable[LocalDate] and
//      (__ \ "countryOfIncorporation").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "foreignCorporateRef").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "address1").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "address2").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "address3").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "address4").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "postcode").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "country").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "adi").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "iomOrCiFlag").readNullable[String].map(_.filter(_.nonEmpty)) and
//      ///////////
//      (__ \ "phoneNumber").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "mobileNumber").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "faxNumber").readNullable[String].map(_.filter(_.nonEmpty)) and
//      (__ \ "businessEmail").readNullable[String].map(_.filter(_.nonEmpty)) and
//      ///////////
//      (__ \ "isFutureLeaveDate").readNullable[Int].map(_.filter(_.nonEmpty)) and
//      (__ \ "isFutureJoinDate").readNullable[Int].map(_.filter(_.nonEmpty)) and
//      (__ \ "businessType").readNullable[Int].map(_.filter(_.nonEmpty))
//  )(PartnerRawData.apply _)

//}
