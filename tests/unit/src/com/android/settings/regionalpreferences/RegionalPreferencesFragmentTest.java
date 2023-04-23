/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.regionalpreferences;

import static org.junit.Assert.assertEquals;

import android.app.settings.SettingsEnums;
import android.os.Looper;

import androidx.test.annotation.UiThreadTest;

import org.junit.Before;
import org.junit.Test;

public class RegionalPreferencesFragmentTest {
    private RegionalPreferencesFragment mFragment;

    @Before
    @UiThreadTest
    public void setUp() throws Exception {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        mFragment = new RegionalPreferencesFragment();
    }

    @Test
    @UiThreadTest
    public void getMetricsCategory_typeIsCa_resultIsCalendarPreference() {
        mFragment.mType = ExtensionTypes.CALENDAR;

        int result = mFragment.getMetricsCategory();

        assertEquals(SettingsEnums.CALENDAR_PREFERENCE, result);
    }

    @Test
    @UiThreadTest
    public void getMetricsCategory_typeIsFw_resultIsFirstDayOfWeekPreference() {
        mFragment.mType = ExtensionTypes.FIRST_DAY_OF_WEEK;

        int result = mFragment.getMetricsCategory();

        assertEquals(SettingsEnums.FIRST_DAY_OF_WEEK_PREFERENCE, result);
    }

    @Test
    @UiThreadTest
    public void getMetricsCategory_typeIsMu_resultIsTemperaturePreference() {
        mFragment.mType = ExtensionTypes.TEMPERATURE_UNIT;

        int result = mFragment.getMetricsCategory();

        assertEquals(SettingsEnums.TEMPERATURE_PREFERENCE, result);
    }
}