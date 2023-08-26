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
package com.diffplug.selfie.junit5

import com.diffplug.selfie.*
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier

internal class ClassMethod(val progress: Progress, val clazz: String, val method: String)

/** Really wish this could be package-private! */
internal object Router {
  fun readOrWrite(snapshot: Snapshot, scenario: String?): Snapshot? {
    val classMethod =
        threadCtx.get()
            ?: throw AssertionError(
                "Selfie `toMatchDisk` must be called only on the original thread.")
    if (RW.isWrite) {
      classMethod.progress.write(classMethod.clazz, classMethod.method, scenario, snapshot)
      return snapshot
    } else {
      return classMethod.progress.read(classMethod.clazz, classMethod.method, scenario)
    }
  }
  val threadCtx = ThreadLocal<ClassMethod?>()
}

internal enum class Usage {
  STARTED,
  SUCCEEDED,
  SKIPPED,
}

internal class SnapshotUsage {
  private var snapshots = ArrayMap.empty<String, Usage>()
  private var file: SnapshotFile? = null
  fun read(): SnapshotFile {
    TODO()
  }
  fun set(method: String, usage: Usage): Unit {
    snapshots = snapshots.plusOrReplace(method) { usage }
  }
  fun finish(classLevelResult: Usage): Unit {}
}

internal class Progress {
  var layout: SelfieLayout? = null
  fun write(clazz: String, method: String, scenario: String?, snapshot: Snapshot) {
    mutate(clazz) {
      val key = if (scenario == null) method else "$method/$scenario"
      it.set(key, Usage.SUCCEEDED)
      it.read().set(key, snapshot)
    }
  }
  fun read(clazz: String, method: String, scenario: String?): Snapshot? {
    return getFrom(clazz) {
      val key = if (scenario == null) method else "$method/$scenario"
      val snapshot = it.read().snapshots.get(key)
      it.set(key, if (snapshot == null) Usage.SKIPPED else Usage.SUCCEEDED)
      return snapshot
    }
  }
  fun start(className: String, method: String?) {
    if (method == null) {
      synchronized(this) { usageByClass = usageByClass.plus(className, SnapshotUsage()) }
    } else {
      mutate(className) {
        it.set(method, Usage.STARTED)
        Router.threadCtx.set(ClassMethod(this, className, method))
      }
    }
  }
  fun skip(className: String, method: String?) {
    if (method != null) {
      mutate(className) { it.set(method, Usage.SKIPPED) }
    }
  }
  fun finish(className: String, method: String?, result: Usage) {
    mutate(className) {
      if (method == null) {
        it.finish(result)
      } else {
        Router.threadCtx.set(null)
        it.set(method, result)
      }
    }
  }

  var usageByClass = ArrayMap.empty<String, SnapshotUsage>()
  private inline fun mutate(clazz: String, mutator: (SnapshotUsage) -> Unit) {
    val usage = synchronized(this) { usageByClass[clazz]!! }
    synchronized(usage) { mutator(usage) }
  }
  private inline fun <T> getFrom(clazz: String, mutator: (SnapshotUsage) -> T): T {
    val usage = synchronized(this) { usageByClass[clazz]!! }
    return synchronized(usage) { mutator(usage) }
  }
}

class SelfieTestExecutionListener : TestExecutionListener {
  private val progress = Progress()
  override fun executionStarted(testIdentifier: TestIdentifier) {
    if (isRoot(testIdentifier)) return
    val (clazz, method) = parseClassMethod(testIdentifier)
    progress.start(clazz, method)
  }
  override fun executionSkipped(testIdentifier: TestIdentifier, reason: String) {
    val (clazz, method) = parseClassMethod(testIdentifier)
    progress.skip(clazz, method)
  }
  override fun executionFinished(
      testIdentifier: TestIdentifier,
      testExecutionResult: TestExecutionResult
  ) {
    val (clazz, method) = parseClassMethod(testIdentifier)
    val result =
        testExecutionResult.status.let {
          when (it) {
            TestExecutionResult.Status.SUCCESSFUL -> Usage.SUCCEEDED
            TestExecutionResult.Status.FAILED,
            TestExecutionResult.Status.ABORTED -> Usage.SKIPPED
            else -> throw IllegalArgumentException("Unknown status $it")
          }
        }
    progress.finish(clazz, method, result)
  }
  private fun isRoot(testIdentifier: TestIdentifier) = testIdentifier.parentId.isEmpty
  private fun parseClassMethod(testIdentifier: TestIdentifier): Pair<String, String?> {
    val display = testIdentifier.displayName
    val pieces = display.split('#')
    assert(pieces.size == 1 || pieces.size == 2) {
      "Expected 1 or 2 pieces, but got ${pieces.size} for $display"
    }
    return if (pieces.size == 1) Pair(pieces[0], null) else Pair(pieces[0], pieces[1])
  }
}
