/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tools;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Provides a suite of utilities for manipulating strings.
 *
 * @author Frz
 * @since Revision 336
 * @version 1.0
 *
 */
public class StringUtil {

    /**
     * Gets a string padded from the left to <code>length</code> by
     * <code>padchar</code>.
     *
     * @param in The input string to be padded.
     * @param padchar The character to pad with.
     * @param length The length to pad to.
     * @return The padded string.
     */
    public static final String getLeftPaddedStr(final String in, final char padchar, final int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int x = in.length(); x < length; x++) {
            builder.append(padchar);
        }
        builder.append(in);
        return builder.toString();
    }

    /**
     * Gets a string padded from the right to <code>length</code> by
     * <code>padchar</code>.
     *
     * @param in The input string to be padded.
     * @param padchar The character to pad with.
     * @param length The length to pad to.
     * @return The padded string.
     */
    public static final String getRightPaddedStr(final String in, final char padchar, final int length) {
        StringBuilder builder = new StringBuilder(in);
        for (int x = in.length(); x < length; x++) {
            builder.append(padchar);
        }
        return builder.toString();
    }

    /**
     * Joins an array of strings starting from string <code>start</code> with a
     * space.
     *
     * @param arr The array of strings to join.
     * @param start Starting from which string.
     * @return The joined strings.
     */
    public static final String joinStringFrom(final String arr[], final int start) {
        return joinStringFrom(arr, start, " ");
    }

    /**
     * Joins an array of strings starting from string <code>start</code> with
     * <code>sep</code> as a seperator.
     *
     * @param arr The array of strings to join.
     * @param start Starting from which string.
     * @return The joined strings.
     */
    public static final String joinStringFrom(final String arr[], final int start, final String sep) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i != arr.length - 1) {
                builder.append(sep);
            }
        }
        return builder.toString();
    }

    /**
     * Makes an enum name human readable (fixes spaces, capitalization, etc)
     *
     * @param enumName The name of the enum to neaten up.
     * @return The human-readable enum name.
     */
    public static final String makeEnumHumanReadable(final String enumName) {
        StringBuilder builder = new StringBuilder(enumName.length() + 1);
        for (String word : enumName.split("_")) {
            if (word.length() <= 2) {
                builder.append(word); // assume that it's an abbrevation
            } else {
                builder.append(word.charAt(0));
                builder.append(word.substring(1).toLowerCase());
            }
            builder.append(' ');
        }
        return builder.substring(0, enumName.length());
    }

    /**
     * Counts the number of <code>chr</code>'s in <code>str</code>.
     *
     * @param str The string to check for instances of <code>chr</code>.
     * @param chr The character to check for.
     * @return The number of times <code>chr</code> occurs in <code>str</code>.
     */
    public static final int countCharacters(final String str, final char chr) {
        int ret = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == chr) {
                ret++;
            }
        }
        return ret;
    }

    public static final String getReadableMillis(long startMillis, long endMillis) {
        StringBuilder sb = new StringBuilder();
        double elapsedSeconds = (endMillis - startMillis) / 1000.0;
        int elapsedSecs = ((int) elapsedSeconds) % 60;
        int elapsedMinutes = (int) (elapsedSeconds / 60.0);
        int elapsedMins = elapsedMinutes % 60;
        int elapsedHrs = elapsedMinutes / 60;
        int elapsedHours = elapsedHrs % 24;
        int elapsedDays = elapsedHrs / 24;
        if (elapsedDays > 0) {
            boolean mins = elapsedHours > 0;
            sb.append(elapsedDays);
            sb.append(" day" + (elapsedDays > 1 ? "s" : "") + (mins ? ", " : "."));
            if (mins) {
                boolean secs = elapsedMins > 0;
                if (!secs) {
                    sb.append("and ");
                }
                sb.append(elapsedHours);
                sb.append(" hour" + (elapsedHours > 1 ? "s" : "") + (secs ? ", " : "."));
                if (secs) {
                    boolean millis = elapsedSecs > 0;
                    if (!millis) {
                        sb.append("and ");
                    }
                    sb.append(elapsedMins);
                    sb.append(" minute" + (elapsedMins > 1 ? "s" : "") + (millis ? ", " : "."));
                    if (millis) {
                        sb.append("and ");
                        sb.append(elapsedSecs);
                        sb.append(" second" + (elapsedSecs > 1 ? "s" : "") + ".");
                    }
                }
            }
        } else if (elapsedHours > 0) {
            boolean mins = elapsedMins > 0;
            sb.append(elapsedHours);
            sb.append(" hour" + (elapsedHours > 1 ? "s" : "") + (mins ? ", " : "."));
            if (mins) {
                boolean secs = elapsedSecs > 0;
                if (!secs) {
                    sb.append("and ");
                }
                sb.append(elapsedMins);
                sb.append(" minute" + (elapsedMins > 1 ? "s" : "") + (secs ? ", " : "."));
                if (secs) {
                    sb.append("and ");
                    sb.append(elapsedSecs);
                    sb.append(" second" + (elapsedSecs > 1 ? "s" : "") + ".");
                }
            }
        } else if (elapsedMinutes > 0) {
            boolean secs = elapsedSecs > 0;
            sb.append(elapsedMinutes);
            sb.append(" minute" + (elapsedMinutes > 1 ? "s" : "") + (secs ? " " : "."));
            if (secs) {
                sb.append("and ");
                sb.append(elapsedSecs);
                sb.append(" second" + (elapsedSecs > 1 ? "s" : "") + ".");
            }
        } else if (elapsedSeconds > 0) {
            sb.append((int) elapsedSeconds);
            sb.append(" second" + (elapsedSeconds > 1 ? "s" : "") + ".");
        } else {
            sb.append("None.");
        }
        return sb.toString();
    }

    public static String secondsToString(long seconds) {
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.DAYS.toHours(day);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.DAYS.toMinutes(day) - TimeUnit.HOURS.toMinutes(hours);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.DAYS.toSeconds(day) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minute);
        String time = "";
        if (day > 0) {
            time += (day + " Days - ");
        }
        if (hours > 0) {
            time += (hours + " Hours - ");
        }
        if (minute > 0) {
            time += (minute + " Minutes - ");
        }
        if (second > 0) {
            time += (second + " Seconds");
        }
        return time;
    }

    public static final int getDaysAmount(long startMillis, long endMillis) {
        double elapsedSeconds = (endMillis - startMillis) / 1000.0;
        int elapsedMinutes = (int) (elapsedSeconds / 60.0);
        int elapsedHrs = elapsedMinutes / 60;
        int elapsedDays = elapsedHrs / 24;
        return elapsedDays;
    }

    public static String getUnitNumber(long value) {
        String text = "" + NumberFormat.getInstance().format(value);
        if (value >= 1000000000000000000L) {
            text = (String.format("%1$,.4f", value / 1000000000000000000.0)) + "BB";
        } else if (value >= 1000000000000000L) {
            text = (String.format("%1$,.2f", value / 1000000000000000.0)) + "MB";
        } else if (value >= 1000000000000L) {
            text = (String.format("%1$,.2f", value / 1000000000000.0)) + "KB";
        } else if (value >= 1000000000) {
            text = (String.format("%1$,.2f", value / 1000000000.0)) + "B";
        } else if (value >= 1000000) {
            text = (String.format("%1$,.2f", value / 1000000.0)) + "M";
        } else if (value >= 1000) {
            text = (String.format("%1$,.1f", value / 1000.0)) + "K";
        }
        return text;
    }

    public static String getUnitFullNumber(long value) {
        String text = "" + NumberFormat.getInstance().format(value);
        if (value >= 1000000000000000000L) {
            text = (String.format("%1$,.1f", value / 1000000000000000000.0)) + "BB";
        } else if (value >= 1000000000000000L) {
            text = (String.format("%1$,.1f", value / 1000000000000000.0)) + "MB";
        } else if (value >= 1000000000000L) {
            text = (String.format("%1$,.1f", value / 1000000000000.0)) + "KB";
        } else if (value >= 1000000000) {
            text = (String.format("%1$,.1f", value / 1000000000.0)) + "B";
        } else if (value >= 1000000) {
            text = (String.format("%1$,.1f", value / 1000000.0)) + "M";
        } else if (value >= 1000) {
            text = (String.format("%1$,.1f", value / 1000.0)) + "K";
        }
        return text;
    }

    public static String getUnitMobNumber(long value) {
        String text = "" + NumberFormat.getInstance().format(value);
        if (value >= 1000000000000000000L) {
            text = (String.format("%1$,.4f", value / 1000000000000000000.0)) + "Quint*";
        } else if (value >= 1000000000000000L) {
            text = (String.format("%1$,.4f", value / 1000000000000000.0)) + " Quad";
        } else if (value >= 1000000000000L) {
            text = (String.format("%1$,.3f", value / 1000000000000.0)) + " Tri";
        } else if (value >= 1000000000) {
            text = (String.format("%1$,.2f", value / 1000000000.0)) + " Bill";
        } else if (value >= 1000000) {
            text = (String.format("%1$,.2f", value / 1000000.0)) + " Mill";
        }
        return text;
    }

    public static String getUnitDamageNumber(BigInteger values) {
        //; -> dot, = -> M, < -> k, > -> b
        String text = "" + NumberFormat.getInstance().format(values);
        if (values.toString().length() > 18) {
            BigInteger[] result = values.divideAndRemainder(BigInteger.valueOf(1000000000000000000L));
            text = "" + result[0] + ";" + result[1].toString().charAt(0) + ">>";
        } else if (values.toString().length() > 15) {
            BigInteger[] result = values.divideAndRemainder(BigInteger.valueOf(1000000000000000L));
            text = "" + result[0] + ";" + result[1].toString().charAt(0) + "=>";
        } else if (values.toString().length() > 12) {
            BigInteger[] result = values.divideAndRemainder(BigInteger.valueOf(1000000000000L));
            text = "" + result[0] + ";" + result[1].toString().charAt(0) + "<>";
        } else if (values.toString().length() > 9) {
            BigInteger[] result = values.divideAndRemainder(BigInteger.valueOf(1000000000L));
            text = "" + result[0] + ";" + result[1].toString().charAt(0) + ">";
        } else if (values.toString().length() > 6) {
            BigInteger[] result = values.divideAndRemainder(BigInteger.valueOf(1000000L));
            text = "" + result[0] + ";" + result[1].toString().charAt(0) + "=";
        } else if (values.toString().length() > 3) {
            BigInteger[] result = values.divideAndRemainder(BigInteger.valueOf(1000L));
            text = "" + result[0] + ";" + result[1].toString().charAt(0) + "<";
        }
        return text;
    }

    //BigDecimal values = new BigDecimal(x);
    //values.divide(BigDecimal.valueOf(1000000000000000L)))
    public static String getUnitBigDamageNumber(BigInteger x) {
        //; -> dot, = -> M, < -> k, > -> b
        BigDecimal values = new BigDecimal(x);
        String text = "";
        if (values.toString().length() > 72) {
            text = values.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000000000000000000L)), 2, RoundingMode.HALF_UP).toString() + ">8";
        } else if (values.toString().length() > 69) {
            text = values.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "=>7";
        } else if (values.toString().length() > 66) {
            text = values.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000000000000L)), 2, RoundingMode.HALF_UP).toString() + "<>7";
        } else if (values.toString().length() > 63) {
            text = values.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000000000L)), 2, RoundingMode.HALF_UP).toString() + ">7";
        } else if (values.toString().length() > 60) {
            text = values.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000000L)), 2, RoundingMode.HALF_UP).toString() + "=>6";
        } else if (values.toString().length() > 57) {
            text = values.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000L)), 2, RoundingMode.HALF_UP).toString() + "<>6";
        } else if (values.toString().length() > 54) {
            text = values.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000000000000L)), 2, RoundingMode.HALF_UP).toString() + ">6";
        } else if (values.toString().length() > 51) {
            text = values.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "=>5";
        } else if (values.toString().length() > 48) {
            text = values.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000000L)), 2, RoundingMode.HALF_UP).toString() + "<>5";
        } else if (values.toString().length() > 45) {
            text = values.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000L)), 2, RoundingMode.HALF_UP).toString() + ">5";
        } else if (values.toString().length() > 42) {
            text = values.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000L)), 2, RoundingMode.HALF_UP).toString() + "=>4";
        } else if (values.toString().length() > 39) {
            text = values.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000L)), 2, RoundingMode.HALF_UP).toString() + "<>4";
        } else if (values.toString().length() > 36) {
            text = values.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000000000000L)), 2, RoundingMode.HALF_UP).toString() + ">4";
        } else if (values.toString().length() > 33) {
            text = values.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "=>3";
        } else if (values.toString().length() > 30) {
            text = values.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000000L)), 2, RoundingMode.HALF_UP).toString() + "<>3";
        } else if (values.toString().length() > 27) {
            text = values.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000L)), 2, RoundingMode.HALF_UP).toString() + ">3";
        } else if (values.toString().length() > 24) {
            text = values.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000L)), 2, RoundingMode.HALF_UP).toString() + "=>2";
        } else if (values.toString().length() > 21) {
            text = values.divide(bbDivisor.multiply(BigDecimal.valueOf(1000L)), 2, RoundingMode.HALF_UP).toString() + "<>2";
        } else if (values.toString().length() > 18) {
            text = values.divide(BigDecimal.valueOf(1000000000000000000L), 1, RoundingMode.HALF_UP).toString() + ">2";
        } else if (values.toString().length() > 15) {
            text = values.divide(BigDecimal.valueOf(1000000000000000L), 1, RoundingMode.HALF_UP).toString() + "=>";
        } else if (values.toString().length() > 12) {
            text = values.divide(BigDecimal.valueOf(1000000000000L), 1, RoundingMode.HALF_UP).toString() + "<>";
        } else if (values.toString().length() > 9) {
            text = values.divide(BigDecimal.valueOf(1000000000L), 1, RoundingMode.HALF_UP).toString() + ">";
        } else if (values.toString().length() > 6) {
            text = values.divide(BigDecimal.valueOf(1000000L), 1, RoundingMode.HALF_UP).toString() + "=";
        } else if (values.toString().length() > 3) {
            text = values.divide(BigDecimal.valueOf(1000L), 1, RoundingMode.HALF_UP).toString() + "<";
        } else {
            text = values.toString();
        }
        return text;
    }

    private static final BigDecimal bbDivisor = BigDecimal.valueOf(1000000000000000000L);
    private static final BigDecimal bbbbDivisor = bbDivisor.multiply(BigDecimal.valueOf(1000000000000000000L));
    private static final BigDecimal bbbbbbDivisor = bbbbDivisor.multiply(BigDecimal.valueOf(1000000000000000000L));

    public static String getUnitBigNumber(BigInteger values) {
        //; -> dot, = -> M, < -> k, > -> b
        BigDecimal x = new BigDecimal(values);
        String text = "";
        if (values.toString().length() > 72) {
            text = x.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "B8";
        } else if (values.toString().length() > 69) {
            text = x.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "MB7";
        } else if (values.toString().length() > 66) {
            text = x.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000000000000L)), 2, RoundingMode.HALF_UP).toString() + "KB7";
        } else if (values.toString().length() > 63) {
            text = x.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000000000L)), 2, RoundingMode.HALF_UP).toString() + "B7";
        } else if (values.toString().length() > 60) {
            text = x.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000000L)), 2, RoundingMode.HALF_UP).toString() + "MB6";
        } else if (values.toString().length() > 57) {
            text = x.divide(bbbbbbDivisor.multiply(BigDecimal.valueOf(1000L)), 2, RoundingMode.HALF_UP).toString() + "KB6";
        } else if (values.toString().length() > 54) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "B6";
        } else if (values.toString().length() > 51) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "MB5";
        } else if (values.toString().length() > 48) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000000L)), 2, RoundingMode.HALF_UP).toString() + "KB5";
        } else if (values.toString().length() > 45) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000L)), 2, RoundingMode.HALF_UP).toString() + "B5";
        } else if (values.toString().length() > 42) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000L)), 2, RoundingMode.HALF_UP).toString() + "MB4";
        } else if (values.toString().length() > 39) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000L)), 2, RoundingMode.HALF_UP).toString() + "KB4";
        } else if (values.toString().length() > 36) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "B4";
        } else if (values.toString().length() > 33) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "MB3";
        } else if (values.toString().length() > 30) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000000L)), 2, RoundingMode.HALF_UP).toString() + "KB3";
        } else if (values.toString().length() > 27) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000L)), 2, RoundingMode.HALF_UP).toString() + "B3";
        } else if (values.toString().length() > 24) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000L)), 2, RoundingMode.HALF_UP).toString() + "MB2";
        } else if (values.toString().length() > 21) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000L)), 2, RoundingMode.HALF_UP).toString() + "KB2";
        } else if (values.toString().length() > 18) {
            text = x.divide(BigDecimal.valueOf(1000000000000000000L), 2, RoundingMode.HALF_UP).toString() + "B2";
        } else if (values.toString().length() > 15) {
            text = x.divide(BigDecimal.valueOf(1000000000000000L), 2, RoundingMode.HALF_UP) + "MB";
        } else if (values.toString().length() > 12) {
            text = x.divide(BigDecimal.valueOf(1000000000000L), 2, RoundingMode.HALF_UP) + "KB";
        } else if (values.toString().length() > 9) {
            text = x.divide(BigDecimal.valueOf(1000000000L), 2, RoundingMode.HALF_UP) + "B";
        } else if (values.toString().length() > 6) {
            text = x.divide(BigDecimal.valueOf(1000000L), 2, RoundingMode.HALF_UP) + "M";
        } else if (values.toString().length() > 3) {
            text = x.divide(BigDecimal.valueOf(1000L), 2, RoundingMode.HALF_UP) + "K";
        } else {
            text = values.toString();
        }
        return text;
    }

    private static String getCharForNumber(int i) {
        return i > 0 && i < 27 ? String.valueOf((char) (i + 64)) : null;
    }

    public static String getUnitBigNumberExpo(BigInteger values) {
        //; -> dot, = -> M, < -> k, > -> b
        BigDecimal x = new BigDecimal(values);
        String text = "";
        if (values.toString().length() > 54) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 51) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 48) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 45) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 42) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 39) {
            text = x.divide(bbbbDivisor.multiply(BigDecimal.valueOf(1000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 36) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 33) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000000000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 30) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 27) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 24) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 21) {
            text = x.divide(bbDivisor.multiply(BigDecimal.valueOf(1000L)), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 18) {
            text = x.divide(BigDecimal.valueOf(1000000000000000000L), 2, RoundingMode.HALF_UP).toString() + "";
        } else if (values.toString().length() > 15) {
            text = x.divide(BigDecimal.valueOf(1000000000000000L), 2, RoundingMode.HALF_UP) + "";
        } else if (values.toString().length() > 12) {
            text = x.divide(BigDecimal.valueOf(1000000000000L), 2, RoundingMode.HALF_UP) + "";
        } else if (values.toString().length() > 9) {
            text = x.divide(BigDecimal.valueOf(1000000000L), 2, RoundingMode.HALF_UP) + "";
        } else if (values.toString().length() > 6) {
            text = x.divide(BigDecimal.valueOf(1000000L), 2, RoundingMode.HALF_UP) + "";
        } else if (values.toString().length() > 3) {
            text = x.divide(BigDecimal.valueOf(1000L), 2, RoundingMode.HALF_UP) + "";
        } else {
            text = values.toString();
        }
        int L = 1 + (int) Math.floor(values.toString().length() * 0.3);
        //System.out.println("L: " + L + " - size: " + values.toString().length());
        text += " (e" + values.toString().length() + ")";
        return text;
    }
}
