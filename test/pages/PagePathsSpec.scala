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

package pages

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{IdxPathNode, KeyPathNode, PathNode}
import queries.{Flag, Gettable}

import java.lang.reflect.Modifier
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters.*
import scala.util.Try

// Every page and query in the project, found through the compiled classes, checked against each other: a new page
// cannot share a path with an existing one, or write inside the value of another page, without this spec noticing.
class PagePathsSpec extends AnyFreeSpec with Matchers {

  import PagePathsSpec.*

  private val found: Seq[Found] = discover()
  private val pages: Seq[Found.Page] = found.collect { case page: Found.Page => page }

  "The pages and queries of the project" - {

    "must all be found and instantiated" in {
      val skipped = found.collect { case skipped: Found.Skipped => skipped }

      info(s"${pages.size} pages and queries instantiated from ${pages.map(_.name).distinct.size} classes")
      skipped.foreach(skipped => info(s"skipped ${skipped.name}: ${skipped.reason}"))

      pages.size must be > 100
      skipped mustBe empty
    }

    "must not share a path between two different pages, apart from the known duplicates" in {
      val shared = sharingAPath.filterNot(group => knownDuplicates.contains(group.map(_.name).toSet))

      shared.map(group => group.map(_.describe).sorted.mkString(" and ")).sorted.foreach(info(_))
      shared mustBe empty
    }

    "must still have every known duplicate sharing a path, or it is no longer one" in {
      knownDuplicates -- sharingAPath.map(_.map(_.name).toSet).toSet mustBe empty
    }

    "must not have a page path inside the value of another page, apart from the known composites" in {
      val nested = for {
        outer <- pages
        inner <- pages
        if outer.name != inner.name && !knownComposites.contains(outer.name) && isInsideValue(outer.nodes, inner.nodes)
      } yield s"${inner.describe} is inside ${outer.describe}"

      nested.distinct.sorted.foreach(info(_))
      nested.distinct mustBe empty
    }

    "must still have pages inside every known composite, or the composite is no longer one" in {
      val stillComposite = knownComposites.filter { name =>
        pages.exists(outer => outer.name == name && pages.exists(inner => inner.name != name && isInsideValue(outer.nodes, inner.nodes)))
      }

      knownComposites -- stillComposite mustBe empty
    }
  }

  // Groups of pages of different classes whose paths are the same
  private def sharingAPath: Seq[Seq[Found.Page]] =
    pages.groupBy(page => normalised(page.nodes)).values.filter(group => group.map(_.name).distinct.size > 1).toSeq
}

object PagePathsSpec {

  // Pages whose value holds the values of other pages. They predate the rule that a page replaces its value whole, and
  // each is to be resolved when its section migrates to the original and modified stores. The section marker pages
  // among them go with that migration.
  val knownComposites: Set[String] = Set(
    "pages.controllingbody.ControllingBodyCorrespondenceSectionPage",
    "pages.licencespremises.PremisesDetailsPage",
    "pages.partnerdetails.PartnerDetailsCorrespondenceDetailsSectionPage",
    "pages.partnerdetails.PartnerDetailsNewCorrespondenceDetailsSectionPage",
    "pages.returnperiods.GamblingReturnPeriodsPage"
  )

  // Different page classes that write to the same path. Two names for one page are a maintenance risk, so this list
  // is here to be emptied, not extended.
  val knownDuplicates: Set[Set[String]] = Set(
    Set("pages.partnerdetails.PartnerDetailsCorrespondenceDetailsSectionPage",
        "pages.partnerdetails.PartnerDetailsNewCorrespondenceDetailsSectionPage"
       )
  )

  // The partner number of an existing partner is a key in the partners object, so it is built with a marker that the
  // path checks treat like a list index
  private val sampleKey = "{key}"

  sealed trait Found {
    def name: String
  }

  object Found {

    final case class Page(name: String, isFlag: Boolean, nodes: List[PathNode]) extends Found {
      def describe: String = s"$name (${if (isFlag) "flag, " else ""}${nodes.map(describeNode).mkString("/")})"
    }

    final case class Skipped(name: String, reason: String) extends Found
  }

  // The compiled classes of the app, found from a class that lives there
  private def discover(): Seq[Found] = {
    val classesDir = Paths.get(classOf[QuestionPage[?]].getProtectionDomain.getCodeSource.getLocation.toURI)
    val loader = classOf[QuestionPage[?]].getClassLoader

    Seq("pages", "queries").flatMap { pkg =>
      val root = classesDir.resolve(pkg)
      classFiles(root).flatMap { file =>
        val className = classesDir.relativize(file).toString.stripSuffix(".class").replace('/', '.').replace('\\', '.')
        Try(Class.forName(className, false, loader)).toOption.toSeq.flatMap(found)
      }
    }
  }

  private def classFiles(root: Path): Seq[Path] =
    if (Files.isDirectory(root)) {
      val stream = Files.walk(root)
      try stream.iterator.asScala.filter(path => path.toString.endsWith(".class")).toSeq
      finally stream.close()
    } else {
      Nil
    }

  // A Scala object is read from its MODULE$ field. A case class is built with 0 for an Int and "key" for a String. A
  // parameter typed String | Int, as the partner pages have, erases to Object and is built both ways, since the path
  // differs between an existing partner (a key) and a new one (an index). Anything else that is a Gettable is reported.
  private def found(cls: Class[?]): Seq[Found] =
    if (!classOf[Gettable[?]].isAssignableFrom(cls) || cls.isInterface || Modifier.isAbstract(cls.getModifiers)) {
      Nil
    } else if (cls.getName.endsWith("$")) {
      Seq(page(cls.getName.stripSuffix("$"), cls.getField("MODULE$").get(null)))
    } else if (cls.getName.contains("$")) {
      Nil // the companion of an object or a nested helper, which has no page of its own
    } else {
      val supported = Set[Class[?]](classOf[Int], classOf[String], classOf[Object])
      cls.getConstructors.find(_.getParameterTypes.forall(supported.contains)) match {
        case Some(constructor) =>
          val variants =
            if (constructor.getParameterTypes.contains(classOf[Object])) Seq(Integer.valueOf(0), sampleKey) else Seq(Integer.valueOf(0))
          variants.map { either =>
            val arguments = constructor.getParameterTypes.map {
              case t if t == classOf[Int]    => Integer.valueOf(0): AnyRef
              case t if t == classOf[String] => sampleKey: AnyRef
              case _                         => either: AnyRef
            }
            page(cls.getName, constructor.newInstance(arguments*))
          }
        case None =>
          Seq(
            Found.Skipped(cls.getName, s"needs ${cls.getConstructors.map(_.getParameterTypes.map(_.getSimpleName).mkString(", ")).mkString(" or ")}")
          )
      }
    }

  private def page(name: String, instance: AnyRef): Found =
    Try(instance.asInstanceOf[Gettable[?]].path.path)
      .fold(error => Found.Skipped(name, s"path failed: ${error.getMessage}"), nodes => Found.Page(name, instance.isInstanceOf[Flag], nodes))

  // Indices do not tell pages apart, as a page in a list is the same page for every element
  private def normalised(nodes: List[PathNode]): List[PathNode] =
    nodes.map {
      case IdxPathNode(_) => IdxPathNode(0)
      case other          => other
    }

  // An index into a list, or the key of a partner in the partners object
  private def isElement(node: PathNode): Boolean =
    node match {
      case IdxPathNode(_)                       => true
      case KeyPathNode(key) if key == sampleKey => true
      case _                                    => false
    }

  // The inner path continues from the outer path with a field, so the inner page writes inside the outer page's value.
  // Continuing with an element is the collection case, where the outer query holds the collection and the inner page
  // one element of it.
  private def isInsideValue(outer: List[PathNode], inner: List[PathNode]): Boolean = {
    val outerNormalised = normalised(outer)
    val innerNormalised = normalised(inner)

    innerNormalised.size > outerNormalised.size &&
    innerNormalised.startsWith(outerNormalised) &&
    !isElement(innerNormalised(outerNormalised.size))
  }

  private def describeNode(node: PathNode): String =
    node match {
      case KeyPathNode(key) => key
      case IdxPathNode(_)   => "[n]"
      case other            => other.toString
    }
}
