/*
 * Copyright (C) 2024 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.selfie.junit5

import io.kotest.matchers.shouldBe
import kotlin.test.Test
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import org.junitpioneer.jupiter.DisableIfTestFails

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@DisableIfTestFails
class InteractiveTest : Harness("undertest-junit5") {
  @Test @Order(1)
  fun initialState() {
    ut_snapshot().deleteIfExists()
    ut_snapshot().assertDoesNotExist()
    ut_mirror().lineWith("expectSelfie(").setContent("    expectSelfie(10).toBe(10)")
    gradleInteractivePass()
  }

  @Test @Order(2)
  fun inlineMismatch() {
    ut_mirror().lineWith("expectSelfie(").setContent("    expectSelfie(5).toBe(10)")
    gradleInteractiveFail().message shouldBe "Inline literal did not match the actual value"
  }

  @Test @Order(3)
  fun inlineMismatchToBe() {
    ut_mirror().lineWith("expectSelfie(").setContent("    expectSelfie(5).toBe_TODO(10)")
    gradleInteractivePass()
    ut_mirror().lineWith("expectSelfie(").content() shouldBe "    expectSelfie(5).toBe(5)"
    gradleInteractivePass()
  }

  @Test @Order(4)
  fun inlineMismatchForeverComment() {
    ut_mirror().lineWith("expectSelfie(").setContent("    expectSelfie(5).toBe(10) // SELFIEWRITE")
    gradleInteractivePass()
    ut_mirror().lineWith("expectSelfie(").content() shouldBe
        "    expectSelfie(5).toBe(5) // SELFIEWRITE"
  }

  @Test @Order(5)
  fun inlineMismatchOnceComment() {
    ut_mirror().lineWith("expectSelfie(").setContent("    expectSelfie(5).toBe(10) // selfieonce")
    gradleInteractivePass()
    ut_mirror().lineWith("expectSelfie(").content() shouldBe "    expectSelfie(5).toBe(5) "
  }

  @Test @Order(6)
  fun diskMismatch() {
    ut_mirror().lineWith("expectSelfie(").setContent("    expectSelfie(\"5\").toMatchDisk()")
    gradleInteractiveFail().message shouldBe "No such snapshot"
  }

  @Test @Order(7)
  fun diskMismatchToBe() {
    ut_mirror().lineWith("expectSelfie(").setContent("    expectSelfie(\"5\").toMatchDisk_TODO()")
    ut_snapshot().deleteIfExists()
    gradleInteractivePass()
    ut_mirror().lineWith("expectSelfie(").content() shouldBe "    expectSelfie(\"5\").toMatchDisk()"

    ut_snapshot()
        .assertContent(
            """
            ╔═ example ═╗
            5
            ╔═ [end of file] ═╗
            
        """
                .trimIndent())
    gradleInteractivePass()
  }

  @Test @Order(8)
  fun diskMismatchForeverComment() {
    ut_snapshot().deleteIfExists()
    ut_mirror()
        .lineWith("expectSelfie(")
        .setContent("    expectSelfie(\"5\").toMatchDisk() //SELFIEWRITE")
    gradleInteractivePass()
    ut_snapshot()
        .assertContent(
            """
            ╔═ example ═╗
            5
            ╔═ [end of file] ═╗
            
        """
                .trimIndent())
    ut_mirror().lineWith("expectSelfie(").content() shouldBe
        "    expectSelfie(\"5\").toMatchDisk() //SELFIEWRITE"
  }

  @Test @Order(9)
  fun diskMismatchOnceComment() {
    ut_snapshot().deleteIfExists()
    ut_mirror()
        .lineWith("expectSelfie(")
        .setContent("    expectSelfie(\"5\").toMatchDisk() //selfieonce")
    gradleInteractivePass()
    ut_snapshot()
        .assertContent(
            """
            ╔═ example ═╗
            5
            ╔═ [end of file] ═╗
            
        """
                .trimIndent())
    ut_mirror().lineWith("expectSelfie(").content() shouldBe
        "    expectSelfie(\"5\").toMatchDisk() "
  }

  @Test @Order(10)
  fun cleanup() {
    ut_snapshot().deleteIfExists()
    ut_mirror().lineWith("expectSelfie(").setContent("    expectSelfie(10).toBe(10)")
  }
}
