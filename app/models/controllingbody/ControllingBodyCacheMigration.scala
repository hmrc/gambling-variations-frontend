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

package models.controllingbody

import models.BusinessType
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

object ControllingBodyCacheMigration {
  def apply(data: JsObject): JsObject = {
    val legacy = (data \ "controllingBodyDetailsSection").asOpt[JsObject]
    val current = (data \ "controllingBodyDetails").asOpt[JsObject]

    (legacy, current) match {
      case (None, None) => data
      case _            =>
        // Replace each answer as a whole, so removed optional name fields stay removed.
        val merged = normalize(legacy.getOrElse(Json.obj())) ++ normalize(current.getOrElse(Json.obj()))
        (data - "controllingBodyDetailsSection") + ("controllingBodyDetails" -> merged)
    }
  }

  private def normalize(section: JsObject): JsObject = {
    val businessType = section.value.get("typeOfControllingBody").orElse(section.value.get("businessType"))
    val renamed = section - "businessType"
    businessType match {
      case Some(value) => renamed + ("typeOfControllingBody" -> normalizeBusinessType(value))
      case None        => renamed
    }
  }

  private def normalizeBusinessType(value: JsValue): JsValue = value match {
    case JsString(name) =>
      BusinessType.values.find(_.toString == name).fold(value)(businessType => Json.toJson(businessType))
    case _ => value
  }
}
