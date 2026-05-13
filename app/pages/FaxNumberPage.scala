package pages

import play.api.libs.json.JsPath

case object FaxNumberPage extends QuestionPage[Boolean] {
  override def path: JsPath = JsPath \ "faxNumber"
}
