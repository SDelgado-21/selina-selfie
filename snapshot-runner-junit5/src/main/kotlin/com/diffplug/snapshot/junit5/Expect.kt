/*
 * Copyright (C) 2023 DiffPlug
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
package com.diffplug.snapshot.junit5

import com.diffplug.snapshot.Snapshot

interface Snapshotter<T> {
  fun snapshot(value: T): Snapshot
}

interface ExpectSnapshot {
  /** Creates a sub-snapshot, which can be used to group related snapshots together. */
  fun scenario(name: String): Expect

  /**
   * Spotless prunes snapshots which are no longer used. If there are some that you want to
   * preserve, perhaps because a single test has different behavior in different environments, then
   * this method will prevent a snapshot from being removed.
   */
  fun preserveScenarios(vararg names: String)
  fun toMatchSnapshotBinary(content: ByteArray): String
  fun toMatchSnapshot(content: Any): String
  fun <T> toMatchSnapshot(content: T, snapshotter: Snapshotter<T>): String
}

interface ExpectLiteral {
  fun toMatchLiteral(content: Int, actual: Int): Int
  fun toMatchLiteral(content: Long, actual: Long): Long
  fun toMatchLiteral(content: Boolean, actual: Boolean): Boolean
  fun toMatchLiteral(content: Any, actual: String): String
}

class Expect : ExpectSnapshot { // TODO: ExpectLiteral
  override fun scenario(name: String) = TODO()
  override fun preserveScenarios(vararg names: String) = TODO()
  override fun toMatchSnapshotBinary(content: ByteArray) = TODO()
  override fun toMatchSnapshot(content: Any) = TODO()
  override fun <T> toMatchSnapshot(content: T, snapshotter: Snapshotter<T>) = TODO()
}
