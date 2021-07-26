package bruce.home;

import lombok.*;

@Data
@Builder
public class LunarMonthData {
    private int heavenlyInt; // 天干數字
    private String heavenly; // 天干中文
    private int earthlyInt; // 地支數字
    private String earthly; // 地支中文
    private boolean isLeapMonth; // 是否閏月
    private int lunarMonth; // 陰曆月
    private int start; // 陰曆開始日期對應陽曆的1號
    private int end; // 陰曆月有幾天
    private int nextEnd; // 下個陰曆月有幾天，一個陽曆月裡有三個陰曆月時會有用(1902/10)
    private int term24Day1; // 第一個24節氣對應的陽曆日期
    private String term24Day1Name;// 第一個24節氣中文
    private int term24Day2; // 第二個24節氣對應的陽曆日期
    private String term24Day2Name; // 第二個24節氣中文
}
