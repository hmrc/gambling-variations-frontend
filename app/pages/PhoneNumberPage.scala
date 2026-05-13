package pages

import play.api.libs.json.JsPath

case object PhoneNumberPage extends QuestionPage[Boolean] {
  override def path: JsPath = JsPath \ "phoneNumber"
}
