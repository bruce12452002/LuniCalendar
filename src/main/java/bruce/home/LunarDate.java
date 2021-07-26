package bruce.home;


import java.time.*;
import java.time.chrono.MinguoDate;
import java.time.temporal.ChronoField;

public final class LunarDate {
    private String heavenly; // 天干
    private String earthly; // 地支
    private boolean isLeapMonth; // 是否閏月
    private int lunarMonth; // 陰曆月
    private int lunarDate; // 陰曆日
    private String term24DayName; // 24節氣名稱

    public String getHeavenly() {
        return heavenly;
    }

    public String getEarthly() {
        return earthly;
    }

    public boolean isLeapMonth() {
        return isLeapMonth;
    }

    public int getLunarMonth() {
        return lunarMonth;
    }

    public int getLunarDate() {
        return lunarDate;
    }

    public String getTerm24DayName() {
        return term24DayName;
    }

    final private String[] heavenly10 = {"甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"};
    final private String[] earthly12 = {"子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"};
    final private String[] solarTerm24 = {"小寒", "大寒", "立春", "雨水", "驚蟄", "春分",
            "清明", "穀雨", "立夏", "小滿", "芒種", "夏至",
            "小暑", "大暑", "立秋", "處暑", "白露", "秋分",
            "寒露", "霜降", "立冬", "小雪", "大雪", "冬至"};

    /**
     * 取得農曆日期
     */
    public LunarDate getLunarDate(LocalDate localDate) {
        // 只支援東八區
        final ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate, LocalTime.of(0, 0), ZoneId.of("Asia/Taipei"));
        return getLunarDate(zonedDateTime.getYear(), zonedDateTime.getMonthValue(), zonedDateTime.getDayOfMonth());
    }

    /**
     * 取得農曆日期
     */
    public LunarDate getLunarDate(ZonedDateTime zonedDateTime) {
        return getLunarDate(zonedDateTime.toLocalDate());
    }

    /**
     * 取得農曆日期
     */
    public LunarDate getLunarDate(OffsetDateTime offsetDateTime) {
        return getLunarDate(offsetDateTime.toZonedDateTime());
    }

    /**
     * 取得農曆日期
     */
    public LunarDate getLunarDate(MinguoDate minguoDate) {
        return getLunarDate(minguoDate.get(ChronoField.YEAR) + 1911,
                minguoDate.get(ChronoField.MONTH_OF_YEAR),
                minguoDate.get(ChronoField.DAY_OF_MONTH));
    }

    /**
     * 取得農曆日期
     */
    private LunarDate getLunarDate(int y, int m, int d) {
        if (y < 1901 || y > 2100) throw new RuntimeException("不支援 1901~2100 以外的農曆日期");
        LunarMonthData lunarMonthData = getLunarMonthData(y, m);

        // 農曆年，農曆的年是立春為第一天
        if (m == 2 && d >= lunarMonthData.getTerm24Day1()) {
            this.heavenly = heavenly10[(lunarMonthData.getHeavenlyInt() + 1) % 10];
            this.earthly = earthly12[Integer.parseInt(Integer.toHexString((lunarMonthData.getEarthlyInt() + 1) % 12))];
        } else {
            this.heavenly = lunarMonthData.getHeavenly();
            this.earthly = lunarMonthData.getEarthly();
        }

        // 農曆月、日
        this.isLeapMonth = lunarMonthData.isLeapMonth();
        this.lunarMonth = lunarMonthData.getLunarMonth();
        this.lunarDate = lunarMonthData.getStart() + (d - 1);
        if (this.lunarDate > lunarMonthData.getEnd()) {
            // 取下個月，不管一個陽曆月有兩個或三個農曆月，都是取下個月
            LunarMonthData nextLunarMonthData = getNextLunarMonthData(y, m);

            if ((d - 1) > lunarMonthData.getNextEnd()) {
                this.isLeapMonth = nextLunarMonthData.isLeapMonth();
                this.lunarMonth = lunarMonth + 2;
                this.lunarDate = 1;
            } else {
                this.isLeapMonth = nextLunarMonthData.isLeapMonth();
                this.lunarMonth++;
                this.lunarDate = this.lunarDate - lunarMonthData.getEnd();
            }
        }

        if (d == lunarMonthData.getTerm24Day1()) this.term24DayName = lunarMonthData.getTerm24Day1Name();
        else if (d == lunarMonthData.getTerm24Day2()) this.term24DayName = lunarMonthData.getTerm24Day2Name();
        return this;
    }

    private LunarMonthData getNextLunarMonthData(int y, int m) {
        final int month = 12;
        int tempMonth = m + 1;
        if (tempMonth > month) {
            m = tempMonth - month;
            y++;
        }
        return getLunarMonthData(y, m);
    }

    private LunarMonthData getLunarMonthData(int y, int m) {
        String[] lunarInfo = LunarMap.lunarMonthData.get("y" + y)[m - 1].split(",");
        final int heavenlyInt = Integer.parseInt(lunarInfo[0].substring(0, 1)); // 天干 0~9
        final int earthlyInt = Integer.parseInt(lunarInfo[0].substring(1)); // 地支(16進制) 0~B
        return LunarMonthData.builder()
                .heavenlyInt(heavenlyInt) // 天干 0~9
                .heavenly(heavenly10[heavenlyInt]) // 天干中文
                .earthlyInt(earthlyInt) // 地支(16進制) 0~B
                .earthly(earthly12[Integer.parseInt(Integer.toHexString(earthlyInt))]) // 地支中文
                .isLeapMonth("1" .equals(lunarInfo[1])) // 是否閏月 0不閏;1閏月
                .lunarMonth(Integer.parseInt(Integer.toHexString(Integer.parseInt(lunarInfo[2])))) // 陰曆月(16進制) 1~C
                .start(Integer.parseInt(lunarInfo[3])) // 陰曆開始日期對應陽曆的1號 1~30
                .end("0" .equals(lunarInfo[4]) ? 29 : 30) // 陰曆月有幾天：0為29天；1為30天
                .nextEnd("0" .equals(lunarInfo[5]) ? 29 : 30) // 下個陰曆月有幾天，一個陽曆月裡有三個陰曆月時會有用(1902/10)
                .term24Day1(Integer.parseInt(lunarInfo[6])) // 第一個24節氣對應的陽曆日期 1~31
                .term24Day1Name(solarTerm24[(m - 1) * 2]) // 第一個24節氣中文
                .term24Day2(Integer.parseInt(lunarInfo[7])) // 第二個24節氣對應的陽曆日期 1~31
                .term24Day2Name(solarTerm24[(m - 1) * 2 + 1]) // 第二個24節氣中文
                .build();
    }

    public static void main(String[] args) {
//        LunarDate lunarDate = new LunarDate().getLunarDate(MinguoDate.now());
        LunarDate lunarDate = new LunarDate().getLunarDate(LocalDate.of(2021, 7, 22));
        System.out.print(lunarDate.getHeavenly());
        System.out.println(lunarDate.getEarthly());
        System.out.println(lunarDate.isLeapMonth());
        System.out.println(lunarDate.getLunarMonth());
        System.out.println(lunarDate.getLunarDate());
        System.out.println(lunarDate.getTerm24DayName());
    }
}
