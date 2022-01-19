package ir.kimia.client.util;

import com.github.mfathi91.time.PersianDate;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static Date convertPersianStringToDate(String dateString) throws DateTimeParseException {

        PersianDate dueDatePersian = null;
        LocalDate dueDateGregorian = null;

        if (StringUtils.isNotEmpty(dateString)) {
            if (dateString.length() == 10) {
                if (dateString.contains("/")) {
                    dueDatePersian = PersianDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                } else if (dateString.contains("-")) {
                    dueDatePersian = PersianDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else {
                    throw new DateTimeParseException("", "", 0);
                }
            } else if (dateString.length() == 8) {
                dueDatePersian = PersianDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
            } else {
                throw new DateTimeParseException("", "", 0);
            }

            dueDateGregorian = dueDatePersian.toGregorian();

        } else {
            throw new DateTimeParseException("", "", 0);
        }

        return java.util.Date.from(dueDateGregorian.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String convertDateToPersianString(Date date, String separator) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        PersianDate persianDate = PersianDate.fromGregorian(localDate);
        int monthValue = persianDate.getMonthValue();
        String monthString = "";
        if (monthValue >= 10) {
            monthString = String.valueOf(monthValue);
        } else {
            monthString = "0" + monthValue;
        }

        int dayOfMonth = persianDate.getDayOfMonth();
        String dayString = "";
        if (dayOfMonth >= 10) {
            dayString = String.valueOf(dayOfMonth);
        } else {
            dayString = "0" + dayOfMonth;
        }
        return persianDate.getYear() + separator + monthString + separator + dayString;
    }

    public static String convertDateToPersianString(Date date) {
        return convertDateToPersianString(date, "/");
    }

    public static String getCurrentPersianDateString() {
        Date date = Calendar.getInstance().getTime();
        return convertDateToPersianString(date);
    }

    public static boolean isGreaterThanToday(String persianDate) {
        Date inputDate = DateUtil.convertPersianStringToDate(persianDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inputDate);
        int inputDay = calendar.get(Calendar.DATE);
        int inputMonth = calendar.get(Calendar.MONTH);
        int inputYear = calendar.get(Calendar.YEAR);

        Date currentDate = new Date();
        Calendar currentCalender = Calendar.getInstance();
        calendar.setTime(currentDate);
        int currentDay = currentCalender.get(Calendar.DATE);
        int currentMonth = currentCalender.get(Calendar.MONTH);
        int currentYear = currentCalender.get(Calendar.YEAR);

        if (inputYear <= currentYear) {
            if (inputMonth <= currentMonth) {
                return !(inputDay <= currentDay);
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); // negative number would decrement the days
        return cal.getTime();
    }

    public static String incrementOrDecrement(String persianDate, int days) {
        if (StringUtils.isEmpty(persianDate)) {
            persianDate = getCurrentPersianDateString();
        }
        Date date = convertPersianStringToDate(persianDate);
        date = addDays(date, days);
        return convertDateToPersianString(date);
    }

    public static String getNextPersianDate(String persianDate) {
        return incrementOrDecrement(persianDate, 1);
    }

    public static String getPreviousPersianDate(String persianDate) {
        return incrementOrDecrement(persianDate, -1);
    }


}


