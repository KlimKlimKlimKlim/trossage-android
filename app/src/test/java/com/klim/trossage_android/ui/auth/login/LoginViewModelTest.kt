package com.klim.trossage_android.ui.auth.login

import com.klim.trossage_android.domain.model.User
import com.klim.trossage_android.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var authRepository: AuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() {
        val state = viewModel.uiState.value

        assertEquals("", state.username)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onUsernameChanged updates username and clears error`() {
        viewModel.onUsernameChanged("testuser")

        val state = viewModel.uiState.value
        assertEquals("testuser", state.username)
        assertNull(state.error)
    }

    @Test
    fun `onPasswordChanged updates password and clears error`() {
        viewModel.onPasswordChanged("password123")

        val state = viewModel.uiState.value
        assertEquals("password123", state.password)
        assertNull(state.error)
    }

    @Test
    fun `login with empty username shows validation error`() {
        viewModel.onUsernameChanged("")
        viewModel.onPasswordChanged("password123")

        viewModel.login {}

        val state = viewModel.uiState.value
        assertEquals("Заполните все поля", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `login with empty password shows validation error`() {
        viewModel.onUsernameChanged("testuser")
        viewModel.onPasswordChanged("")

        viewModel.login {}

        val state = viewModel.uiState.value
        assertEquals("Заполните все поля", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `login with short username shows validation error`() {
        viewModel.onUsernameChanged("ab")
        viewModel.onPasswordChanged("password123")

        viewModel.login {}

        val state = viewModel.uiState.value
        assertEquals("Логин должен быть от 3 до 20 символов", state.error)
    }

    @Test
    fun `login with short password shows validation error`() {
        viewModel.onUsernameChanged("testuser")
        viewModel.onPasswordChanged("pass")

        viewModel.login {}

        val state = viewModel.uiState.value
        assertEquals("Пароль должен быть от 8 до 63 символов", state.error)
    }

    @Test
    fun `successful login updates state correctly`() = runTest {
        val testUser = User("1", "testuser", "Test User")
        coEvery { authRepository.login("testuser", "password123") } returns Result.success(testUser)

        viewModel.onUsernameChanged("testuser")
        viewModel.onPasswordChanged("password123")

        var onSuccessCalled = false
        viewModel.login { onSuccessCalled = true }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(onSuccessCalled)
        coVerify { authRepository.login("testuser", "password123") }
    }

    @Test
    fun `failed login shows error message`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception("Invalid credentials"))

        viewModel.onUsernameChanged("testuser")
        viewModel.onPasswordChanged("wrongpassword")

        viewModel.login {}

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Invalid credentials", state.error)
    }

    @Test
    fun `login sets isLoading to true during request`() = runTest {
        coEvery { authRepository.login(any(), any()) } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.success(User("1", "test", "Test"))
        }

        viewModel.onUsernameChanged("testuser")
        viewModel.onPasswordChanged("password123")

        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.login {}

        testScheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
    }
}
