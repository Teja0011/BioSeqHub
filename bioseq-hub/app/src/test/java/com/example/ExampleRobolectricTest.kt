package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.common.BioinformaticsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BioSeq Hub", appName)
    }

    @Test
    fun `test gc content calculation`() {
        val dna = "GCGCATAT"
        val gc = BioinformaticsEngine.calculateGcContent(dna)
        assertEquals(50.0, gc, 0.01)
    }

    @Test
    fun `test needleman wunsch sequence alignment`() {
        val seq1 = "MEEPQSDPSVEPPLSQETFS"
        val seq2 = "MEEPQSDPSVEPPLSQETFS"
        val alignment = BioinformaticsEngine.needlemanWunsch(seq1, seq2)
        assertEquals(100.0, alignment.identityPercent, 0.01)
        assertTrue(alignment.score > 0)
    }

    @Test
    fun `test reverse complement`() {
        val dna = "ATGC"
        val revComp = BioinformaticsEngine.reverseComplement(dna)
        assertEquals("GCAT", revComp)
    }
}
