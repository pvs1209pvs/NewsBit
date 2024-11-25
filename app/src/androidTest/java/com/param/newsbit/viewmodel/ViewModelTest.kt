package com.param.newsbit.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.param.newsbit.getOrAwaitValueTest
import com.param.newsbit.model.parser.NetworkStatus
import com.param.newsbit.repo.RepositoryInterface
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.stubbing.Answer

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: RepositoryInterface

    private lateinit var viewModel: ViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun returnErrorStatusWhenRepositoryFailsToDownloadNews() = runTest {

        viewModel = ViewModel(
            repository,
            ApplicationProvider.getApplicationContext(),
            StandardTestDispatcher(testScheduler)
        )

        `when`(
            repository.downloadNews(
                "Top Stories",
                20
            )
        ).thenThrow(IllegalStateException::class.java)

        viewModel.downloadNews("Top Stories", 20)

        advanceUntilIdle()

        Assert.assertEquals(
            NetworkStatus.ERROR,
            viewModel.downloadNewsError.getOrAwaitValueTest()
        )

    }

    @Test
    fun returnSuccessStatusWhenRepositoryFailsToDownloadNews() = runTest {

        viewModel = ViewModel(
            repository,
            ApplicationProvider.getApplicationContext(),
            StandardTestDispatcher(testScheduler)
        )

        `when`(repository.downloadNews("Top Stories", 20)).thenReturn(20)

        viewModel.downloadNews("Top Stories", 20)

        advanceUntilIdle()

        Assert.assertEquals(
            NetworkStatus.SUCCESS,
            viewModel.downloadNewsError.getOrAwaitValueTest()
        )

    }

    @Test
    fun returnErrorStatusWhenChatGPTServiceFails() = runTest {

        viewModel = ViewModel(
            repository,
            ApplicationProvider.getApplicationContext(),
            StandardTestDispatcher(testScheduler)
        )

        `when`(repository.downloadSummary("https://www.thestar.com/0"))
            .thenThrow(IllegalStateException::class.java)

        viewModel.downloadSummary("https://www.thestar.com/0")

        advanceUntilIdle()

        Assert.assertEquals(
            NetworkStatus.ERROR,
            viewModel.downloadSummaryError.getOrAwaitValueTest()
        )

    }

    @Test
    fun returnSuccessStatusWhenChatGPTServiceTerminatesSuccessfully() = runTest {

        viewModel = ViewModel(
            repository,
            ApplicationProvider.getApplicationContext(),
            StandardTestDispatcher(testScheduler)
        )

        `when`(repository.downloadSummary("https://www.thestar.com/0")).then { }

        viewModel.downloadSummary("https://www.thestar.com/0")

        advanceUntilIdle()

        Assert.assertEquals(
            NetworkStatus.SUCCESS,
            viewModel.downloadSummaryError.getOrAwaitValueTest()
        )

    }

    @Test
    fun returnErrorStatusWhenSummaryFailsToRefresh() = runTest {

        viewModel = ViewModel(
            repository,
            ApplicationProvider.getApplicationContext(),
            StandardTestDispatcher(testScheduler)
        )

        `when`(repository.refreshSummary("https://www.thestar.com/0"))
            .thenThrow(IllegalStateException::class.java)

        viewModel.refreshSummary("https://www.thestar.com/0")

        advanceUntilIdle()

        Assert.assertEquals(
            NetworkStatus.ERROR,
            viewModel.refreshSummaryError.getOrAwaitValueTest()
        )

    }

    @Test
    fun returnSuccessStatusWhenSummaryIsRefreshed() = runTest {

        viewModel = ViewModel(
            repository,
            ApplicationProvider.getApplicationContext(),
            StandardTestDispatcher(testScheduler)
        )

        `when`(repository.refreshSummary("https://www.thestar.com/0")).then { }

        viewModel.refreshSummary("https://www.thestar.com/0")

        advanceUntilIdle()

        Assert.assertEquals(
            NetworkStatus.SUCCESS,
            viewModel.refreshSummaryError.getOrAwaitValueTest()
        )

    }

}