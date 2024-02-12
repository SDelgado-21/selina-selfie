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
package com.diffplug.selfie.kotest

import com.diffplug.selfie.guts.FS
import com.diffplug.selfie.guts.TypedPath
import io.kotest.assertions.Actual
import io.kotest.assertions.Exceptions
import io.kotest.assertions.Expected
import io.kotest.assertions.print.Printed
import okio.FileSystem
import okio.Path.Companion.toPath

expect internal val FS_SYSTEM: FileSystem
internal fun TypedPath.toPath(): okio.Path = absolutePath.toPath()

internal object FSOkio : FS {
  /** Walks the files (not directories) which are children and grandchildren of the given path. */
  override fun <T> fileWalk(typedPath: TypedPath, walk: (Sequence<TypedPath>) -> T): T =
      walk(
          FS_SYSTEM.listRecursively(typedPath.toPath()).mapNotNull {
            if (FS_SYSTEM.metadata(it).isRegularFile) TypedPath.ofFile(it.toString()) else null
          })
  override fun fileRead(typedPath: TypedPath): String =
      FS_SYSTEM.read(typedPath.toPath()) { readUtf8() }
  override fun fileWrite(typedPath: TypedPath, content: String): Unit =
      FS_SYSTEM.write(typedPath.toPath()) { writeUtf8(content) }
  /** Creates an assertion failed exception to throw. */
  override fun assertFailed(message: String, expected: Any?, actual: Any?): Throwable =
      if (expected == null && actual == null) Exceptions.createAssertionError(message, null)
      else {
        val expectedStr = nullableToString(expected, "")
        val actualStr = nullableToString(actual, "")
        if (expectedStr.isEmpty() && actualStr.isEmpty() && (expected == null || actual == null)) {
          // make sure that nulls are not ambiguous
          val onNull = "(null)"
          comparisonAssertion(
              message, nullableToString(expected, onNull), nullableToString(actual, onNull))
        } else {
          comparisonAssertion(message, expectedStr, actualStr)
        }
      }
  private fun nullableToString(any: Any?, onNull: String): String =
      any?.let { it.toString() } ?: onNull
  private fun comparisonAssertion(message: String, expected: String, actual: String): Throwable {
    return Exceptions.createAssertionError(
        message, null, Expected(Printed((expected))), Actual(Printed((actual))))
  }
}
