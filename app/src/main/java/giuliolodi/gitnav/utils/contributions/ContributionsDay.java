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

public class ContributionsDay {

    public int year = Integer.MIN_VALUE;
    public int month = Integer.MIN_VALUE;
    public int day = Integer.MIN_VALUE;

    // Level is used to record the color of the block
    public int level = Integer.MIN_VALUE;
    // Data is used to calculated the height of the pillar
    public int data = Integer.MIN_VALUE;

    public ContributionsDay(int year, int month, int day, int level, int data) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.level = level;
        this.data = data;
    }
}
