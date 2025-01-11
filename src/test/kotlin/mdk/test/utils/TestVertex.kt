package mdk.test.utils

import mdk.gsm.util.IStringVertex

open class TestVertex(override val id: String) : IStringVertex

class SubTestVertex(stepId: String) : TestVertex(stepId) {
    val testField : Boolean = true
}