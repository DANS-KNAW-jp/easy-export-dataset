/*******************************************************************************
  * Copyright 2015 DANS - Data Archiving and Networked Services
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/

package nl.knaw.dans.easy

import java.io.{InputStream, FileOutputStream, File}

import org.apache.commons.io.IOUtils

import scala.util.{Success, Failure, Try}
import scala.xml._

package object export {

  def invert[T1,T2] (m: Map[T1,T2]): Map[T2,T1] =
    m.map{case (key,value) => (value,key)}


  def toSdoDir(objectId: String)(implicit s: Settings): File =
    new File(s.sdoSet, objectId.replaceAll("[^0-9a-zA-Z]", "_"))


  def write(bytes: Array[Byte], f: File): Try[Unit] =
    Try{IOUtils.write(bytes,new FileOutputStream(f))}

  def copyAndClose(in: InputStream, f: File
           ): Try[Unit] =
    try{
      val out = new FileOutputStream(f)
      try{
        IOUtils.copyLarge(in,out)
        Success(Unit)
      } finally {
        IOUtils.closeQuietly(out)
      }
    } finally {
      IOUtils.closeQuietly(in)
    }

  def readXmlAndClose (is: InputStream): Success[Elem] =
    try{
      Success(XML.load(is))
    } finally {
      IOUtils.closeQuietly(is)
    }


  def read(inputStream: InputStream
          ): Success[Array[Byte]] = {
    try{
      Success(IOUtils.toByteArray(inputStream))
    }finally {
      IOUtils.closeQuietly(inputStream)
    }
  }

  case class RichSeq[T](self: Seq[T]) extends Seq[T] {

    // TODO apply the magic of org.scalatest.Matchers.AnyShouldWrapper.should
    // so we can get arround the constructor

    /** Executes f for each T until one fails.
      * Usage: RichSeq(Xs).foreachUntilFailure((x: T) => f(x))
      * */
    def foreachUntilFailure[S](f: T => Try[S]): Try[Unit] = {
      self.foreach { x =>
        f(x).recover { case t: Throwable => return Failure(t) }
      }
      Success(Unit)
    }

    override def length = self.length
    override def apply(idx: Int): T = self.apply(idx)
    override def iterator: Iterator[T] = self.iterator
  }
}
