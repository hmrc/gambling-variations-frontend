package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class ChooseReturnPeriodsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "ChooseReturnPeriods" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ChooseReturnPeriods.values.toSeq)

      forAll(gen) { chooseReturnPeriods =>

        JsString(chooseReturnPeriods.toString).validate[ChooseReturnPeriods].asOpt.value mustEqual chooseReturnPeriods
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ChooseReturnPeriods.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>

        JsString(invalidValue).validate[ChooseReturnPeriods] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ChooseReturnPeriods.values.toSeq)

      forAll(gen) { chooseReturnPeriods =>

        Json.toJson(chooseReturnPeriods) mustEqual JsString(chooseReturnPeriods.toString)
      }
    }
  }
}
