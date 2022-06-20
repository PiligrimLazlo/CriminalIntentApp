package ru.pl.criminalintent

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.pl.criminalintent.details.CrimeDetailsFragment


@RunWith(AndroidJUnit4::class)
class CrimeDetailsFragmentTest {

    private lateinit var scenario: FragmentScenario<CrimeDetailsFragment>

    @Before
    fun setUp() {
        scenario = launchFragmentInContainer()
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun isCheckBoxWithFalseInitialStateChangesIsSolvedCrimeField() {
        onView(withId(R.id.crime_solved)).check(matches(isNotChecked()))
        scenario.onFragment {
            //assertFalse(it.crime.isSolved)
        }

        onView(withId(R.id.crime_solved)).perform(ViewActions.click())

        scenario.onFragment {
            //assertTrue(it.crime.isSolved)
        }
    }

    @Test
    fun isEditTextWithEmptyInitialStateChangesTitleCrimeField() {
        onView(withId(R.id.crime_title)).check(matches(withText("")))

        onView(withId(R.id.crime_title)).perform(ViewActions.typeText("test text"))

        scenario.onFragment {
            //assertEquals("test text", it.crime.title)
        }
    }
}