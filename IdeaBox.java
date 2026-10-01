import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

// 데이터 모델: 감상평 항목
class IdeaItem {
    private static final String DEFAULT_VALUE = "없음";

    private final String title;
    private final String category;
    
    private String note; // 'overallReview'에서 'note'로 변수명까지 Clean Code 원칙에 맞게 수정
    private String genre;
    private String theme;
    private String directing;
    private String scenario;

    // 1번 메뉴에서 Note를 즉시 입력받기 위한 생성자
    public IdeaItem(String title, String category, String note) {
        this(title, category, note, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE);
    }

    public IdeaItem(String title, String category, String note, String genre, String theme, String directing, String scenario) {
        this.title = title;
        this.category = category;
        this.note = (note == null || note.isEmpty()) ? DEFAULT_VALUE : note;
        this.genre = genre;
        this.theme = theme;
        this.directing = directing;
        this.scenario = scenario;
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }

    public void updateNote(String note) { 
        this.note = (note == null || note.isEmpty()) ? DEFAULT_VALUE : note; 
    }
    
    public void updateDetailedReview(String genre, String theme, String directing, String scenario) {
        this.genre = genre;
        this.theme = theme;
        this.directing = directing;
        this.scenario = scenario;
    }

    public String toCsvRow() {
        return String.join(",", title, category, note, genre, theme, directing, scenario);
    }

    @Override
    public String toString() {
        return String.format(
            "----------------------------------------\n" +
            "작품명 : %s [%s]\n" +
            "Note  : %s\n" +
            "상세평 -> 장르: %s | 주제: %s | 연출: %s | 시나리오: %s\n" +
            "----------------------------------------",
            title, category, note, genre, theme, directing, scenario
        );
    }
}

// 메인 컨트롤러
public class IdeaBox {
    private static final String FILE_NAME = "ideas.csv";
    private static final Map<String, IdeaItem> ideaMap = new HashMap<>();
    private static final Console console = System.console();

    public static void main(String[] args) {
        loadFromCsv();
        runApplication();
    }

    private static void runApplication() {
        boolean isRunning = true;
        while (isRunning) {
            printMenu();
            String input = getUserInput();

            switch (input) {
                case "1": createItem(); break;
                case "2": viewAndReviewCategory(); break;
                case "3": searchByName(); break;
                case "4": addDetailedReview(); break;
                case "5":
                    exitApplication();
                    isRunning = false;
                    break;
                default:
                    System.out.println("[오류] 1~5 사이의 숫자를 입력해주세요.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n========================================");
        System.out.println("             Idea Box (v1.0)            ");
        System.out.println("========================================");
        System.out.println("1. 항목 생성 (매체, 작품명 및 Note 등록)");
        System.out.println("2. 그룹 조회 및 Note 수정");
        System.out.println("3. 통합 이름 검색");
        System.out.println("4. 감상평 세분화 (장르/주제/연출 등)");
        System.out.println("5. 종료");
        System.out.println("========================================");
        System.out.print("메뉴를 선택하세요 >> ");
    }

    // [1] 항목 생성 (사용 구조 개선: 그룹명 입력 후 제목까지 받도록 수정)
    private static void createItem() {
        System.out.print("\n매체 그룹(게임, 애니, 영화, #부가 추가 가능) 입력: ");
        String category = getUserInput();
        
        System.out.print("작품명 입력: ");
        String title = getUserInput();

        if (ideaMap.containsKey(title)) {
            System.out.println("[안내] 이미 등록된 작품명입니다.");
            return;
        } 
        
        System.out.print("Note: ");
        String note = getUserInput();
        
        ideaMap.put(title, new IdeaItem(title, category, note));
        saveToCsv();
        System.out.println("[성공] '" + title + "' 항목 및 Note가 등록 완료되었습니다!");
    }

    // [2] 그룹 조회 및 Note 수정
    private static void viewAndReviewCategory() {
        System.out.print("\n조회할 매체 그룹 이름 입력: ");
        String category = getUserInput();
        
        List<IdeaItem> foundItems = findItemsByCategory(category);
        
        if (foundItems.isEmpty()) {
            System.out.println("[안내] 해당 그룹에 조회할 데이터가 없습니다.");
            return;
        }

        foundItems.forEach(System.out::println);

        System.out.print("\nNote를 추가/수정할 작품명을 정확히 입력하세요 (취소하려면 엔터): ");
        String title = getUserInput();
        if (title.isEmpty()) return;

        processNoteUpdate(title, category);
    }

    private static List<IdeaItem> findItemsByCategory(String category) {
        List<IdeaItem> result = new ArrayList<>();
        for (IdeaItem item : ideaMap.values()) {
            if (item.getCategory().equals(category)) {
                result.add(item);
            }
        }
        return result;
    }

    private static void processNoteUpdate(String title, String category) {
        IdeaItem selectedItem = ideaMap.get(title);
        
        if (selectedItem != null && selectedItem.getCategory().equals(category)) {
            System.out.print("Note: ");
            selectedItem.updateNote(getUserInput());
            saveToCsv();
            System.out.println("[성공] Note 등록 완료!");
        } else {
            System.out.println("[오류] 해당 그룹에 없는 작품명입니다.");
        }
    }

    // [3] 통합 검색
    private static void searchByName() {
        System.out.print("\n검색할 작품명 키워드 입력: ");
        String keyword = getUserInput();
        boolean found = false;

        for (IdeaItem item : ideaMap.values()) {
            if (item.getTitle().contains(keyword)) {
                System.out.println(item);
                found = true;
            }
        }

        if (!found) System.out.println("[안내] 검색 결과가 없습니다.");
    }

    // [4] 감상평 세분화
    private static void addDetailedReview() {
        System.out.print("\n상세평을 등록할 작품명 입력: ");
        String title = getUserInput();

        IdeaItem item = ideaMap.get(title);
        if (item == null) {
            System.out.println("[오류] 조회할 데이터가 없습니다.");
            return;
        }

        System.out.print("장르 느낌: ");
        String genre = getUserInput();
        System.out.print("주제 의식: ");
        String theme = getUserInput();
        System.out.print("연출 방식: ");
        String directing = getUserInput();
        System.out.print("시나리오 평가: ");
        String scenario = getUserInput();
        
        item.updateDetailedReview(genre, theme, directing, scenario);
        saveToCsv();
        System.out.println("[성공] 상세평 등록 완료!");
    }

    private static void exitApplication() {
        saveToCsv();
        System.out.println("데이터를 안전하게 저장했습니다. 프로그램을 종료합니다.");
    }

    private static String getUserInput() {
    if (console == null) {
        throw new IllegalStateException("콘솔 입력을 사용할 수 없습니다.");
    }

    String input = console.readLine();
    return input == null ? "" : input.trim().replace(",", " ");
}

    private static void loadFromCsv() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length == 7) {
                    ideaMap.put(data[0], new IdeaItem(data[0], data[1], data[2], data[3], data[4], data[5], data[6]));
                }
            }
            System.out.println("[시스템] 기존 데이터를 성공적으로 불러왔습니다.");
        } catch (IOException e) {
            System.out.println("[오류] 데이터 로드 실패: " + e.getMessage());
        }
    }

    private static void saveToCsv() {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_NAME), StandardCharsets.UTF_8))) {
            for (IdeaItem item : ideaMap.values()) {
                bw.write(item.toCsvRow());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("[오류] 데이터 저장 실패: " + e.getMessage());
        }
    }
}