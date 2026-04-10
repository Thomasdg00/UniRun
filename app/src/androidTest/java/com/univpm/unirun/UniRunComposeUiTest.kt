package com.univpm.unirun

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.univpm.unirun.ui.auth.AuthScreen
import com.univpm.unirun.ui.components.StatCard
import com.univpm.unirun.ui.theme.UniRunTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class UniRunComposeUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun authScreen_showsErrorAndInvokesLogin() {
        var loginClicks = 0

        composeRule.setContent {
            UniRunTheme {
                AuthScreen(
                    email = "athlete@unirun.app",
                    onEmailChange = {},
                    password = "Secret123",
                    onPasswordChange = {},
                    onLogin = { loginClicks++ },
                    onGoogleSignInRequest = {},
                    onForgotPassword = {},
                    onRegister = {},
                    isLoading = false,
                    errorMessage = "Email o password non valida"
                )
            }
        }

        composeRule.onNodeWithText("ACCEDI").performClick()
        composeRule.onNodeWithText("Email o password non valida").assert(hasText("Email o password non valida"))
        composeRule.runOnIdle {
            assertEquals(1, loginClicks)
        }
    }

    @Test
    fun statCard_rendersCoreContent() {
        composeRule.setContent {
            UniRunTheme {
                StatCard(
                    title = "Distance",
                    value = "10.5 km",
                    subtitle = "This week"
                )
            }
        }

        composeRule.onNodeWithText("DISTANCE").assert(hasText("DISTANCE"))
        composeRule.onNodeWithText("10.5 km").assert(hasText("10.5 km"))
        composeRule.onNodeWithText("This week").assert(hasText("This week"))
    }
}
