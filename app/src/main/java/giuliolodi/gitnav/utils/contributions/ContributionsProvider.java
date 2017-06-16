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

import java.util.ArrayList;
import java.util.List;

public class ContributionsProvider {

    public final static String FILL_STRING = "fill=\"";
    public final static String DATA_STRING = "data-count=\"";
    public final static String DATE_STRING = "data-date=\"";

    public List<ContributionsDay> getContributions(String string) {
        ArrayList<ContributionsDay> contributions = new ArrayList<>();
        int fillPos = -1;
        int dataPos = -1;
        int datePos = -1;
        while (true) {
            fillPos = string.indexOf(FILL_STRING, fillPos + 1);
            dataPos = string.indexOf(DATA_STRING, dataPos + 1);
            datePos = string.indexOf(DATE_STRING, datePos + 1);

            if (fillPos == -1) break;

            int level = 0;
            String levelString
                    = string.substring(fillPos + FILL_STRING.length(),
                    fillPos + FILL_STRING.length() + 7);
            switch (levelString) {
                case "#ebedf0": level = 0; break;
                case "#c6e48b": level = 1; break;
                case "#7bc96f": level = 2; break;
                case "#239a3b": level = 3; break;
                case "#196127": level = 4; break;
            }

            int dataEndPos = string.indexOf("\"", dataPos + DATA_STRING.length());
            String dataString = string.substring(dataPos + DATA_STRING.length(), dataEndPos);
            int data = Integer.valueOf(dataString);

            String dateString
                    = string.substring(datePos + DATE_STRING.length(),
                    datePos + DATE_STRING.length() + 11);

            contributions.add(new ContributionsDay(
                    Integer.valueOf(dateString.substring(0, 4)),
                    Integer.valueOf(dateString.substring(5, 7)),
                    Integer.valueOf(dateString.substring(8, 10)),
                    level,
                    data
            ));
        }

        return contributions;
    }
}
