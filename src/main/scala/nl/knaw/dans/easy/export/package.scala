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

import java.io.File

import scala.util.{Success, Failure, Try}

package object export {

  def invert[T1,T2] (m: Map[T1,T2]): Map[T2,T1] =
    m.map{case (k,v) => (v,k)}

  def honestWrite(f: File, s: String): Try[Unit] =
    Try{scala.tools.nsc.io.File(f).writeAll(s)}

  /** Executes f for each T until one fails.
    * Usage: foreachUntilFailure(triedXs, (x: T) => f(x))
    * */
  def foreachUntilFailure[T,S](xs: Seq[T], f: T=> Try[S]):Try[Unit] =
    {
      xs.foreach(x =>
        f(x).recover { case t: Throwable =>
          return Failure(t)
        }
      )
      Success(Unit)
    }

  case class RichSeq[T](self: Seq[T]) extends Seq[T] {

    // TODO apply the magic of org.scalatest.Matchers.AnyShouldWrapper.should
    // so we can get arround the constructor

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
