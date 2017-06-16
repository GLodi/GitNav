/*
 * Copyright 2017 GLodi
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

package giuliolodi.gitnav.utils.contributions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by giulio on 16/06/2017.
 */

public class DatesUtils {

    /**
     * Get the day of week from a date.
     * 0 for SUN.
     * 1 for MON.
     * .
     * .
     * .
     * 6 for SAT.
     *
     * @param year The year of the date.
     * @param month The month of the date.
     * @param day The day of month of the date.
     * @return Integer to determine the day of week.
     */
    public static int getWeekDayFromDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        calendar.add(Calendar.SECOND, 0);
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     * Get the short month name for a certain date.
     *
     * @param year The year of the date.
     * @param month The month of the date.
     * @param day The day of the date.
     * @return The short name of the month.
     */
    public static String getShortMonthName(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        calendar.add(Calendar.SECOND, 0);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM", Locale.US);
        return month_date.format(calendar.getTime());
    }

    /**
     * Return if the date given is a first week of mount
     *
     * @param year The year of the date.
     * @param month The month of the date.
     * @param day The day of the date.
     * @return true or false
     */
    public static boolean isFirstWeekOfMount(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);
        calendar.add(Calendar.SECOND, 0);
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);

        return calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 1;
    }

    /**
     * Return if the date given is a first day of week
     *
     * @param year The year of the date.
     * @param month The month of the date.
     * @param day The day of the date.
     * @return true or false
     */
    public static boolean isFirstDayOfWeek(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);
        calendar.add(Calendar.SECOND, 0);
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);

        return calendar.get(Calendar.DAY_OF_WEEK) == 1;
    }

}