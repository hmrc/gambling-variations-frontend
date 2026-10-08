package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

//TODO
class NonStandardReturnPeriodsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "ReturnPeriods" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(NonStandardReturnPeriodsOptions.values.toSeq)

      forAll(gen) {
        returnPeriods =>

          JsString(returnPeriods.toString).validate[NonStandardReturnPeriodsOptions].asOpt.value mustEqual returnPeriods
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!NonStandardReturnPeriodsOptions.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[NonStandardReturnPeriodsOptions] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(NonStandardReturnPeriodsOptions.values.toSeq)

      forAll(gen) {
        returnPeriods =>

          Json.toJson(returnPeriods) mustEqual JsString(returnPeriods.toString)
      }
    }
  }
}
