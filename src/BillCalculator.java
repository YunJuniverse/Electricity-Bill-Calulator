import java.math.BigDecimal;
import java.util.Scanner;
import java.util.regex.*;

import static java.lang.Math.*;

public class BillCalculator {
    /** 용어 정리
     * 기본요금(Demand Charge): double demandCharge
     * 전력량(Electronical Energy): double energy
     * 전력량요금(Energy Charge): double energyCharge
     * 기후환경요금(Clmate Charge & Environmental Charge): double climateCharge
     * 연료비조정요금(Fuel Cost Pass-Through Adjustment Rate): double fuelCostCharge
     * 전기요금(Electricity Tariff): int elecTariff
     * 부가가치세(Value-added Tax, VAT): double vat
     * 전력사업기반기금(Electrical Industry Foundation Fund): double industryFund
     * 청구 요금(Electricity Bill): int elecBill
     */

    /** 전기 요금(Electricity Tariff)
     * 기본요금 + 전력량요금 + 기후환경요금 +- 연료비조정요금
     * 기후환경요금 단가: 9원/kWh('23년 1월 기준, 매 년 변동)
     * 연료비조정요금 단가: 5원/kWh('23년 1분기 기준, 매 분기 변동)
     */

    /** 청구 금액(Electricity Bill)
     * 전기요금(기본요금 + 전력량요금 + 기후환경요금 +- 연료비조정요금) + 부가가치세 + 전력사업기반기금
     * 부가가치세(원미만 4사 5입): 전기요금 X 10%
     * 전력사업기반기금(10원 미만 절사): 전기요금 X 3.7%
     */

    // 데이터셋
    private static boolean season = false; // 하계 true, 나머지 false.
    private static double demandCharge = 0;
    private static double energyCharge = 0;
    private static double energy = 0;
    private static double climateCharge = 9;
    private static double fuelCostCharge = 5;
    private static double elecTariff = 0;
    private static double elecBill = 0;

    // vat();
    // industryFund();

    public static void main(String[] args) {
        /** 1. 콘솔입력 기능 */
        System.out.println("==Electricity Bill Calculator(low voltage level for residence)==");
        System.out.println("2023년 1월 산정 기준");
        System.out.println("다음과 같이 입력하세요. \"n월 전력량(kWh)\" 날짜(월)과 전력량은 띄어쓰기를 합니다.");
        System.out.println("예) 6월달 전력량이 321kWh인 경우 -> \"6월 321\"");

        Scanner input = new Scanner(System.in);
        String inputValue = input.nextLine();

        String pattern = "^[0-9]*월 [0-9]*$";
        boolean regex = Pattern.matches(pattern, inputValue);
        //예외처리
        if(!regex){
            System.out.println("잘못된 입력입니다. 올바른 예: 2월 27");
            System.out.println("날짜(월)와 전력량 사이에 띄어쓰기 1개가 들어가야 합니다.");
        }

        //콘솔 입력값에 따른 데이터셋 값 적용(계절, 전력량)
        String[] parts = inputValue.split(" ");
        String seasonFromConsol = parts[0];
        String energyFromConsol = parts[1];


        if (seasonFromConsol.equals("7월") || seasonFromConsol.equals("8월")) season = true;
        energy = Double.parseDouble(energyFromConsol);

        /** 2. 월별 구간별 기본요금-추가요금 나누기 */
        charge(season);

        /** 3. 기후환경요금 / 연료비조정요금 계산 */
        climateCharge = climateCharge * energy;
        fuelCostCharge = fuelCostCharge * energy;

        /** 4. 전기 요금 계산 */
        elecTariff = demandCharge + (energy * energyCharge) + climateCharge + fuelCostCharge;

        /** 5. 청구금액 계산 */
        elecBill = elecTariff + vat(elecTariff) + industryFund(elecTariff);

        // 10원미만 절사
        elecBill = elecBill * 0.1;
        elecBill = floor(elecBill) * 10;

        /** 6. 콘솔출력 */
        String stringForSeason;
        if (season) stringForSeason = "하계";
        else stringForSeason = "일반";


        System.out.println("계절: " + stringForSeason);
        System.out.println("전력량: " + energy + "kWh");
        System.out.println("기본요금: " + demandCharge);
        System.out.println("kWh당 전력량 요금: " + energyCharge);
        System.out.println("총 전력량 요금: " + energy * energyCharge);
        System.out.println("기후환경요금: " + climateCharge);
        System.out.println("연료비조정요금: " + fuelCostCharge);
        System.out.println("전기요금: " + elecTariff);
        System.out.println("부가가치세: " + vat(elecTariff));
        System.out.println("전력사업기반기금: " + industryFund(elecTariff));
        System.out.println("첨구금액: " + elecBill);
    }

    /** 기본요금, 구간별 전력 요금 계산 메서드 */
    public static void charge(boolean season){
        if(season){ // 하계
            if(energy <= 300){
                demandCharge = 910;
                energyCharge = 112;
            } else if(energy <= 450){
                demandCharge = 1600;
                energyCharge = 206.6;
            } else if(energy > 450){
                demandCharge = 7300;
                energyCharge = 299.3;
            }
        } else { // 기타 계절
            if(energy <= 200){
                demandCharge = 910;
                energyCharge = 112;
            } else if(energy <= 400){
                demandCharge = 1600;
                energyCharge = 206.6;
            } else if(energy > 400){
                demandCharge = 7300;
                energyCharge = 299.3;
            }
        }
    }

    /** 부가가치세(원미만 4사 5입): 전기요금 X 10% */
    public static double vat(double elecTariff){
        double vatResult = round(elecTariff * 0.1);
        return vatResult;
    }

    /** 전력사업기반기금(10원 미만 절사): 전기요금 X 3.7% */
    public static double industryFund(double elecTariff){
        /*
        BigDecimal cutOff = new BigDecimal(elecTariff  * 0.037);
        cutOff =cutOff.setScale(-1, BigDecimal.ROUND_FLOOR); // 10원 미만 절사
        double fundResult = cutOff.doubleValue(); // double로 형 변환
         */
        double fundResult = elecTariff  * 0.037;
        fundResult = round(fundResult * 0.1) * 10;

        return fundResult;
    }
}