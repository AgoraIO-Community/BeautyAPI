/*
 * MIT License
 *
 * Copyright (c) 2023 Agora Community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.agora.beautyapi.sensetime.utils.processor;

import android.util.Log;

import java.util.ArrayList;
import java.util.LongSummaryStatistics;

/**
 * @Description
 * @Author Lu Guoqiang
 * @Time 6/7/21 5:50 PM
 */
public class CostTimeUtils {

    private static ArrayList<Long> runTimesList = new ArrayList<>();

    // 50次的平均耗时
    private static final int MAX_RUN_COUNT = 100;

    public static void printAverage(final String TAG, long runTime) {
        if (runTimesList == null) runTimesList = new ArrayList<>();
        if (runTimesList.size() < MAX_RUN_COUNT) runTimesList.add(runTime);
        if (runTimesList.size() == MAX_RUN_COUNT) {
            double averageTime;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                LongSummaryStatistics stats = runTimesList.stream().mapToLong((x) -> x).summaryStatistics();
                averageTime = stats.getAverage();
            } else {
                long sumTime = 0;
                for (Long time : runTimesList) {
                    sumTime = sumTime + time;
                }
                averageTime = sumTime / MAX_RUN_COUNT;
            }
            Log.i(TAG, "human action cost time average:" + averageTime);
            runTimesList.clear();
        }
    }
}
