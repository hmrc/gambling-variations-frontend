package models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class ReturnPeriodsChoiceSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "ReturnPeriodsChoice" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ReturnPeriodsChoice.values.toSeq)

      forAll(gen) { returnPeriodsChoice =>

        JsString(returnPeriodsChoice.toString).validate[ReturnPeriodsChoice].asOpt.value mustEqual returnPeriodsChoice
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ReturnPeriodsChoice.values.map(_.toString).contains(_))

      forAll(gen) { invalidValue =>

        JsString(invalidValue).validate[ReturnPeriodsChoice] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ReturnPeriodsChoice.values.toSeq)

      forAll(gen) { returnPeriodsChoice =>

        Json.toJson(returnPeriodsChoice) mustEqual JsString(returnPeriodsChoice.toString)
      }
    }
  }
}
