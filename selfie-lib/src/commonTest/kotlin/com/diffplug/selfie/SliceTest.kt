/*
 * Copyright (C) 2020-2023 DiffPlug
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
package com.diffplug.selfie

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SliceTest {
  @Test
  fun afterTest() {
    val abcdef = Slice.of("abcdef")
    val untilA = abcdef.until("a")
    untilA.toString() shouldBe ""
    abcdef.after(untilA).toString() shouldBe "abcdef"
    val untilC = abcdef.until("c")
    untilC.toString() shouldBe "ab"
    abcdef.after(untilC).toString() shouldBe "cdef"
    val untilF = abcdef.until("f")
    untilF.toString() shouldBe "abcde"
    abcdef.after(untilF).toString() shouldBe "f"
    val untilZ = abcdef.until("z")
    untilZ.toString() shouldBe "abcdef"
    abcdef.after(untilZ).toString() shouldBe ""
  }

  @Test
  fun unixLine() {
    Slice.of("A single line").unixLine(1).toString() shouldBe "A single line"
    val oneTwoThree = Slice.of("\nI am the first\nI, the second\n\nFOURTH\n")
    oneTwoThree.unixLine(1).toString() shouldBe ""
    oneTwoThree.unixLine(2).toString() shouldBe "I am the first"
    oneTwoThree.unixLine(3).toString() shouldBe "I, the second"
    oneTwoThree.unixLine(4).toString() shouldBe ""
    oneTwoThree.unixLine(5).toString() shouldBe "FOURTH"
    oneTwoThree.unixLine(6).toString() shouldBe ""
  }
}
