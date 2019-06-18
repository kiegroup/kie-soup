/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.soup.commons.cron;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CronExpressionTest {

    private static final String[] VALID_EXPRESSIONS = {
            "0 15 10 * * ? 2005",
            "0 0 0 1 * ?",
            "19 15 10 4 Apr ?",
            "0 43 9 ? * 5L",
            "0 0 0 ? * 4#1 *",
            "0 0 0 ? * * *",
            "0 0 12 */7 * ?",
            "* 0,12 0,13 1W MAY ? *",
            "0 0 0 ? * WED *",
            "0 0,1 2-22 1W * ? *",
            "9/5 0,1 3 LW * ? *",
            "* 4 3 ? 1 4L *"
    };

    private static final String[] INVALID_VALID_EXPRESSIONS = {
            "* * * * Foo ?",
            "* * * * Jan-Foo ?",
            "0 0 * * * *",
            "0 0 * 4 * *",
            "0 0 * * * 4",
            "0 43 9 1,5,29,L * ?",
            "0 43 9 ? * SAT,SUN,L",
            "0 43 9 ? * 6,7,L",
            "0 0 0 L-1 * ? *",
            "* * 0 *",
            "0/1 43 9 ? * 6,7,L",
            "0/a 43 9 ? * 6,7,L",
            "0 0 12 * * THU",
            "# # # # # #",
            "0 0 102 2 * ?",
            "425 0 1 2 * ?",
            "0 0 12 */q * ?",
            "0 0 12 * * THU,WED-SAT",
    };

    @Test
    public void testCronValidExpression() {
        Arrays.stream(VALID_EXPRESSIONS).forEach(expression -> assertTrue(CronExpression.isValidExpression(expression)));
    }

    @Test
    public void testCronInvalidExpression() {
        Arrays.stream(INVALID_VALID_EXPRESSIONS).forEach(expression -> assertFalse(CronExpression.isValidExpression(expression)));
    }

    @Test
    public void testToString() throws Exception {
        assertEquals("0 15 10 * * ? 2005", new CronExpression("0 15 10 * * ? 2005").toString());
    }

    @Test
    public void testGetCronExpression() throws Exception {
        assertEquals("0 15 10 * * ? 2005", new CronExpression("0 15 10 * * ? 2005").getCronExpression());
    }

    @Test
    public void testGetExpressionSummary() throws Exception {
        String value1 = "8 15 10 25 3 ? 2005";
        String expectedSummary1 = "seconds: 8" + "\n" +
                "minutes: 15" + "\n" +
                "hours: 10" + "\n" +
                "daysOfMonth: 25" + "\n" +
                "months: 3" + "\n" +
                "daysOfWeek: ?" + "\n" +
                "lastdayOfWeek: false" + "\n" +
                "nearestWeekday: false" + "\n" +
                "NthDayOfWeek: 0" + "\n" +
                "lastdayOfMonth: false" + "\n" +
                "years: 2005" + "\n";
        assertEquals(expectedSummary1, new CronExpression(value1).getExpressionSummary());

        String value2 = "5 10 20 ? * MON,WED 2015";
        String expectedSummary2 = "seconds: 5" + "\n" +
                "minutes: 10" + "\n" +
                "hours: 20" + "\n" +
                "daysOfMonth: ?" + "\n" +
                "months: *" + "\n" +
                "daysOfWeek: 2,4" + "\n" +
                "lastdayOfWeek: false" + "\n" +
                "nearestWeekday: false" + "\n" +
                "NthDayOfWeek: 0" + "\n" +
                "lastdayOfMonth: false" + "\n" +
                "years: 2015" + "\n";

        assertEquals(expectedSummary2, new CronExpression(value2).getExpressionSummary());
    }
}
