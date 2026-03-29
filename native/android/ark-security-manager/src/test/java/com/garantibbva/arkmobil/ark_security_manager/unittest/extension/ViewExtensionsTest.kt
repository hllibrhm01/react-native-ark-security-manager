package com.garantibbva.arkmobil.ark_security_manager.unittest.extension

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.garantibbva.arkmobil.securitymanager.extensions.activity
import com.garantibbva.arkmobil.securitymanager.extensions.removeExistingByTag
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class ViewExtensionsTest {

    @MockK(relaxed = true)
    lateinit var mockContextActivity: Activity

    @MockK(relaxed = true)
    lateinit var mockView: View

    @MockK(relaxed = true)
    lateinit var mockChild: View

    @MockK(relaxed = true)
    lateinit var mockViewGroup: ViewGroup

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `activity extension returns activity from context extension`() {
        every { mockView.context } returns mockContextActivity

        val result = mockView.activity

        assertSame(mockContextActivity, result)
    }

    @Test
    fun `removeExistingByTag removes child when tag matches and parent is same`() {
        val tag = "tag1"
        every { mockViewGroup.findViewWithTag<View>(tag) } returns mockChild
        every { mockChild.parent } returns mockViewGroup

        mockViewGroup.removeExistingByTag(tag)

        verify { mockViewGroup.removeView(mockChild) }
    }

    @Test
    fun `removeExistingByTag does nothing when no child with tag`() {
        val tag = "missingTag"
        every { mockViewGroup.findViewWithTag<View>(tag) } returns null

        mockViewGroup.removeExistingByTag(tag)

        verify(inverse = true) { mockViewGroup.removeView(any()) }
    }

    @Test
    fun `removeExistingByTag does nothing when child parent is different`() {
        val tag = "tag2"
        val otherParent = mockk<ViewGroup>()

        every { mockViewGroup.findViewWithTag<View>(tag) } returns mockChild
        every { mockChild.parent } returns otherParent

        mockViewGroup.removeExistingByTag(tag)

        verify(inverse = true) { mockViewGroup.removeView(any()) }
    }
}
