package pages

import play.api.libs.json.JsPath

case object MobileNumberPage extends QuestionPage[Boolean] {
  override def path: JsPath = JsPath \ "mobileNumber"
}
