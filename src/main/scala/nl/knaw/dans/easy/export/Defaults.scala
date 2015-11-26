/** *****************************************************************************
  * Copyright 2015 DANS - Data Archiving and Networked Services
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * *****************************************************************************/

package nl.knaw.dans.easy.export

import java.io.File

import org.apache.commons.configuration.PropertiesConfiguration

import scala.collection.JavaConverters._
import scala.util.{Failure, Try}

object Defaults {

  def getDefaultsOfArgsNotSpecified(
                                     specifiedArgs: Seq[String],
                                     defaultsFile: File,
                                     optionMap: Map[String, Char]): Try[Seq[String]] = Try {
    def inArgs(key: String): Boolean = {
      val longArgs = specifiedArgs.filter(_.matches("--.*")).map(_.replaceFirst("--", ""))
      val shortArgs = specifiedArgs.filter(_.matches("-[^-].*")).map(_.charAt(1))
      longArgs.contains(key) || shortArgs.contains(optionMap.getOrElse(key, null))
    }

    checkReadable(defaultsFile)
    val props = new PropertiesConfiguration(defaultsFile)

    def keyValuePair(key: String): Array[String] =
      Array(s"--${key.replace("default.", "")}", props.getString(key))

    if (specifiedArgs.contains("--help") || specifiedArgs.contains("--version")) Array[String]()
    else props.getKeys.asScala.toList
      .filter(key => key.startsWith("default.") && !inArgs(key.replace("default.", "")))
      .flatMap(key => keyValuePair(key))
  }

  private def checkReadable(f: File) = if (!f.isFile || !f.canRead) throw new IllegalArgumentException(s"$f is not a readable file")
}
