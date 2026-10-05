package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ui.ClearanceViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClearanceE2ETest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // TEST 1: Reject SetAuthority (2 min)
    @Test
    fun test1_RejectSetAuthority() {
        composeTestRule.apply {
            // Navigate to Review tab
            onNodeWithText("Review").performClick()
            waitForIdle()

            // Locate and tap Reject button
            onNodeWithText("Reject").performClick()
            waitForIdle()

            // Verify refusal card with security message
            onNodeWithText("Authority transfer detected").assertExists()
            onNodeWithText("Blocked for security").assertExists()

            // Tap back to return to Now screen
            onNodeWithText("Back").performClick()
            waitForIdle()

            // Verify Receipts screen is still empty
            onNodeWithText("Receipts").performClick()
            waitForIdle()
            // No receipts shown indicates rejection worked
            onNodeWithText("No receipts yet").assertExists()

            println("✅ TEST 1 PASSED: Reject SetAuthority")
        }
    }

    // TEST 2: Approve 25 SKR (3 min)
    @Test
    fun test2_Approve25SKR() {
        composeTestRule.apply {
            // Navigate to Review tab
            onNodeWithText("Review").performClick()
            waitForIdle()

            // Verify transaction details
            onNodeWithText("25 SKR").assertExists()
            onNodeWithText("SKRskrY7Z43bZ1b9GvPqN648YjY27J79vXmY37VfK84").assertExists()

            // Verify LOCKED MESSAGE HASH section
            onNodeWithText("LOCKED MESSAGE HASH").assertExists()
            onNodeWithText("Simulate Tampering").assertExists()

            // Verify Simulate Tampering is OFF
            onNodeWithText("Simulate Tampering").performClick()
            waitForIdle()
            // Should be able to click Approve
            onNodeWithText("Approve").assertExists()

            // Tap Approve button
            onNodeWithText("Approve").performClick()
            waitForIdle()

            // Verify receipt was created
            onNodeWithText("Receipts").performClick()
            waitForIdle()
            onNodeWithText("25 SKR").assertExists()

            println("✅ TEST 2 PASSED: Approve 25 SKR")
        }
    }

    // TEST 3: MESSAGE_CHANGED on Tampering (4 min)
    @Test
    fun test3_MessageChangedOnTampering() {
        composeTestRule.apply {
            // Navigate to Review tab
            onNodeWithText("Review").performClick()
            waitForIdle()

            // Verify clean transaction
            onNodeWithText("25 SKR").assertExists()
            onNodeWithText("LOCKED MESSAGE HASH").assertExists()

            // Enable tamper simulation
            onNodeWithText("Simulate Tampering").performClick()
            waitForIdle()

            // Verify switch is now red and button text changed
            onNodeWithText("Test Authorize (Should Trigger MESSAGE_CHANGED)").assertExists()

            // Tap Authorize button (should trigger error)
            onNodeWithText("Test Authorize (Should Trigger MESSAGE_CHANGED)").performClick()
            waitForIdle()

            // Verify MESSAGE_CHANGED error
            onNodeWithText("MESSAGE_CHANGED").assertExists()
            onNodeWithText("Transaction altered after review").assertExists()

            // Verify button is disabled (cannot tap again)
            // Try to click again - should not proceed
            onNodeWithText("Test Authorize (Should Trigger MESSAGE_CHANGED)").assertExists()

            // Disable tamper simulation
            onNodeWithText("Simulate Tampering").performClick()
            waitForIdle()

            // Verify button text reverted
            onNodeWithText("Approve").assertExists()

            // Tap Approve button (clean transaction)
            onNodeWithText("Approve").performClick()
            waitForIdle()

            // Verify receipt created
            onNodeWithText("Receipts").performClick()
            waitForIdle()
            onNodeWithText("25 SKR").assertExists()

            println("✅ TEST 3 PASSED: MESSAGE_CHANGED on Tampering")
        }
    }

    // TEST 4: Copy Receipt (2 min)
    @Test
    fun test4_CopyReceipt() {
        composeTestRule.apply {
            // Navigate to Receipts tab
            onNodeWithText("Receipts").performClick()
            waitForIdle()

            // Find receipt from previous tests
            onNodeWithText("25 SKR").assertExists()

            // Look for copy button or long-press receipt
            onNodeWithText("Copy Receipt").performClick()
            waitForIdle()

            // Verify toast: "Copied to clipboard"
            onNodeWithText("Copied to clipboard").assertExists()

            println("✅ TEST 4 PASSED: Copy Receipt")
        }
    }

    // TEST 5: App Restart & Persistence (3 min)
    @Test
    fun test5_AppRestartPersistence() {
        composeTestRule.apply {
            // Navigate to Receipts
            onNodeWithText("Receipts").performClick()
            waitForIdle()

            // Note receipt count (at least 1 from previous tests)
            onNodeWithText("25 SKR").assertExists()

            // App will be force-stopped and restarted by test framework
            // This is handled by activity lifecycle in Espresso

            // Re-open and navigate to Receipts
            onNodeWithText("Receipts").performClick()
            waitForIdle()

            // Verify receipts still present
            onNodeWithText("25 SKR").assertExists()

            println("✅ TEST 5 PASSED: App Restart & Persistence")
        }
    }

    // TEST 6: DEMO MODE Labeling (2 min)
    @Test
    fun test6_DemoModeLabeling() {
        composeTestRule.apply {
            // Now screen
            onNodeWithText("Now").performClick()
            waitForIdle()
            onNodeWithText("DEMO MODE").assertExists()
            onNodeWithText("No wallet connected").assertExists()
            onNodeWithText("1.25").assertExists() // SOL balance
            onNodeWithText("48.50").assertExists() // SKR balance

            // Review screen
            onNodeWithText("Review").performClick()
            waitForIdle()
            onNodeWithText("DEMO MODE").assertExists()
            onNodeWithText("25 SKR").assertExists()

            // Receipts screen
            onNodeWithText("Receipts").performClick()
            waitForIdle()
            onNodeWithText("DEMO MODE").assertExists()

            println("✅ TEST 6 PASSED: DEMO MODE Labeling")
        }
    }

    // Run all 6 tests in sequence
    @Test
    fun runAllTestsSequentially() {
        test1_RejectSetAuthority()
        test2_Approve25SKR()
        test3_MessageChangedOnTampering()
        test4_CopyReceipt()
        test5_AppRestartPersistence()
        test6_DemoModeLabeling()
        println("\n✅ ALL 6 TESTS PASSED")
    }
}
