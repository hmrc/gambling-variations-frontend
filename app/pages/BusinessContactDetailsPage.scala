package pages

import play.api.libs.json.JsPath

case object BusinessContactDetailsPage extends QuestionPage[Boolean] {
  override def path: JsPath = JsPath \ "businessContactDetails"
}