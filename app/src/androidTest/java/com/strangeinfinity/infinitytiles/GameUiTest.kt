package com.strangeinfinity.infinitytiles

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testMainScreenDisplaysGameTitleAndDeveloperBrand() {
        // Assert game title 2048-Infinity is displayed
        composeTestRule.onNodeWithText("2048-Infinity").assertIsDisplayed()

        // Assert tagline is displayed
        composeTestRule.onNodeWithText("Merge. Think. Go Beyond.").assertIsDisplayed()

        // Assert Score & Best cards are displayed
        composeTestRule.onNodeWithText("SCORE").assertIsDisplayed()
        composeTestRule.onNodeWithText("BEST").assertIsDisplayed()

        // Assert Developer attribution is displayed
        composeTestRule.onNodeWithText("Swipe to merge tiles • Developed by StrangeInfinity").assertIsDisplayed()
    }
}
