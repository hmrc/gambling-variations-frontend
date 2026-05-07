package pages
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.JsPath

class BusinessTypePageSpec extends AnyFreeSpec with Matchers {

  ".BusinessTypePage" - {

    "must have the correct toString" in {
      BusinessTypePage.toString mustBe "businessType"
    }

    "must have a path corresponding to its name" in {
      val expectedPath: JsPath = JsPath \ "businessType"
      BusinessTypePage.path mustBe expectedPath
    }
  }
}
