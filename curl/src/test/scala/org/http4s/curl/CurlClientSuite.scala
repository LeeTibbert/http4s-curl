/*
 * Copyright 2022 http4s.org
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

package org.http4s.curl

import cats.effect.IO
import cats.effect.SyncIO
import cats.effect.kernel.Resource
import cats.effect.unsafe.IORuntime
import cats.syntax.all._
import munit.CatsEffectSuite
import org.http4s.Method._
import org.http4s.Request
import org.http4s.client.Client
import org.http4s.curl.unsafe.CurlRuntime
import org.http4s.syntax.all._

class CurlClientSuite extends CatsEffectSuite {

  override lazy val munitIoRuntime: IORuntime = CurlRuntime()

  val clientFixture: SyncIO[FunFixture[Client[IO]]] = ResourceFunFixture(
    Resource.eval(CurlClient.get)
  )

  clientFixture.test("3 get echos") { client =>
    client
      .expect[String]("https://postman-echo.com/get")
      .map(_.nonEmpty)
      .assert
      .parReplicateA_(3)
  }

  clientFixture.test("3 post echos") { client =>
    IO.randomUUID
      .flatMap { uuid =>
        val msg = s"hello postman $uuid"
        client
          .expect[String](
            Request[IO](POST, uri = uri"https://postman-echo.com/post").withEntity(msg)
          )
          .map(_.contains(msg))
          .assert
      }
      .parReplicateA_(3)

  }

}
