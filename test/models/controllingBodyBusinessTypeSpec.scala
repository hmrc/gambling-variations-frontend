package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class controllingBodyBusinessTypeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "controllingBodyBusinessType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(controllingBodyBusinessType.values.toSeq)

      forAll(gen) {
        controllingBodyBusinessType =>

          JsString(controllingBodyBusinessType.toString).validate[controllingBodyBusinessType].asOpt.value mustEqual controllingBodyBusinessType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!controllingBodyBusinessType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[controllingBodyBusinessType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(controllingBodyBusinessType.values.toSeq)

      forAll(gen) {
        controllingBodyBusinessType =>

          Json.toJson(controllingBodyBusinessType) mustEqual JsString(controllingBodyBusinessType.toString)
      }
    }
  }
}
