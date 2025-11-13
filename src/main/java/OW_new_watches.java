import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class OW_new_watches {

    public static Properties locator;
    public static Properties config;
    public static Map<Integer, String> elements;
    public static List<WebElement> product;
    public static List<WebElement> caseSizeList;
    public static List<WebElement> caseMaterialList;
    public static WebDriver driver;
    public static BufferedWriter wr;
    public static String productName, caseName, caseMaterialName, tradePrice;
    public static int caseSize = 0, caseMaterialSize = 0, productSize = 0, sno = 1, datafetched = 0;
    public static int productCounter = 0, caseSizeCounter = 0, caseMaterialCounter = 0, totalCounter = 0, totalCombinations = 0;
    public static String fileName;
    public static int inputValue;

    public static void main(String[] args) throws IOException, InterruptedException {

        locator = new Properties();
        locator.load(new FileInputStream("src/locators.properties"));

        config = new Properties();
        config.load(new FileInputStream("src/config.properties"));

        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get(config.getProperty("URL"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

        driver.findElement(By.xpath(locator.getProperty("watches"))).click();

        System.out.println("Enter number accordingly: \n1 for Apple watch\n2 for Samsung watches\n3 for Garmin watches");
        inputValue = new Scanner(System.in).nextInt();

        elements = new HashMap<>();
        elements.put(1, locator.getProperty("appleWatch"));
        elements.put(2, locator.getProperty("samsungWatch"));
        elements.put(3, locator.getProperty("garminWatches"));

        WebElement selected = driver.findElement(By.xpath(elements.get(inputValue)));
        fileName = selected.getText().trim().replaceAll(" ", "") + ".csv";
        selected.click();

        wr = new BufferedWriter(new FileWriter(fileName, false));
        wr.write("SNO,Product Name,Case Size,Case Material,Trade in Value");
        wr.newLine();
        wr.close();

        tempProductLoop();
        if (driver != null) caseSizeStartLoop();
    }

    // ---------------- PRODUCT LOOP ----------------
    public static void tempProductLoop() throws IOException, InterruptedException {
        product = driver.findElements(By.xpath(locator.getProperty("productList")));
        productSize = product.size();

        if (productCounter >= productSize) {
            System.out.println("✅ All products fetched successfully!");
            driver.quit();
            driver = null;
            return;
        }

        productName = product.get(productCounter).getText();
        System.out.println("\n📱 Product " + (productCounter + 1) + ": " + productName);

        driver.findElements(By.xpath(locator.getProperty("selectOption"))).get(productCounter).click();
        Thread.sleep(300);

        caseSizeList = driver.findElements(By.xpath(locator.getProperty("caseSize")));
        if (caseSizeList.size() == 0) {
            saveNoData();
            productResetAndNext();
        } else {
            calculateTotalCombinations();
        }
    }

    // ---------------- COUNT TOTAL VARIATIONS ----------------
    public static void calculateTotalCombinations() throws InterruptedException {
        caseSizeList = driver.findElements(By.xpath(locator.getProperty("caseSize")));
        for (int i = 0; i < caseSizeList.size(); i++) {
            caseSizeList.get(i).click();
            Thread.sleep(200);

            caseMaterialList = driver.findElements(By.xpath(locator.getProperty("caseMaterialSize")));
            totalCombinations += caseMaterialList.size();
        }

        System.out.println("Total Variations: " + totalCombinations);

        driver.findElement(By.xpath(locator.getProperty("cancel"))).click();
        Thread.sleep(300);

        driver.findElements(By.xpath(locator.getProperty("selectOption"))).get(productCounter).click();
        Thread.sleep(300);
    }

    // ---------------- CASE SIZE LOOP ----------------
    public static void caseSizeStartLoop() throws IOException, InterruptedException {
        caseSizeList = driver.findElements(By.xpath(locator.getProperty("caseSize")));
        caseSize = caseSizeList.size();
        caseSizeCounter = 0;

        for (int i = 0; i < caseSize; i++) {
            caseSizeCounter++;
            caseSizeList = driver.findElements(By.xpath(locator.getProperty("caseSize")));
            caseName = caseSizeList.get(i).getText();
            System.out.println("   ├── Case Size: " + caseName);
            caseSizeList.get(i).click();
            Thread.sleep(200);

            caseMaterialStartLoop(i);
        }
    }

    // ---------------- CASE MATERIAL LOOP ----------------
    public static void caseMaterialStartLoop(int caseIndex) throws InterruptedException, IOException {
        caseMaterialList = driver.findElements(By.xpath(locator.getProperty("caseMaterialSize")));
        caseMaterialSize = caseMaterialList.size();
        caseMaterialCounter = 0;

        for (int j = 0; j < caseMaterialSize; j++) {
            caseMaterialCounter++;
            totalCounter++;

            caseMaterialList = driver.findElements(By.xpath(locator.getProperty("caseMaterialSize")));
            caseMaterialName = caseMaterialList.get(j).getText();

            System.out.println("      ├── Material: " + caseMaterialName);
            caseMaterialList.get(j).findElement(By.xpath("./button")).click();
            Thread.sleep(100);

            driver.findElement(By.xpath(locator.getProperty("doneButton"))).click();
            yesAndPrintPrice();

            System.out.println(" Progress: " + totalCounter + " / " + totalCombinations);

            if (totalCounter >= totalCombinations) {
                resetAllAfterProduct();
                return;
            }

            reopenTillCaseMaterial(caseIndex);
        }
    }

    // ---------------- PRICE FETCH ----------------
    public static void yesAndPrintPrice() throws IOException, InterruptedException {

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        while (true) {
            try {
                driver.findElement(By.xpath(locator.getProperty("yesButton"))).click();
            } catch (Exception e) {
                break;
            }
        }

        tradePrice = driver.findElement(By.xpath(locator.getProperty("priceText"))).getText();
        System.out.println("         💰 PRICE: " + tradePrice);

        writeAllData();

        driver.findElement(By.xpath(locator.getProperty("cancel"))).click();
        Thread.sleep(500);
    }

    // ---------------- SAVE TO CSV ----------------
    private static void writeAllData() throws IOException {
        wr = new BufferedWriter(new FileWriter(fileName, true));
        wr.write(sno + "," + productName + "," + caseName + "," + caseMaterialName + "," + tradePrice);
        wr.newLine();
        sno++;
        wr.close();
    }

    private static void saveNoData() throws IOException {
        wr = new BufferedWriter(new FileWriter(fileName, true));
        wr.write(sno + "," + productName + ",NO DATA,NO DATA,NO DATA");
        wr.newLine();
        sno++;
        wr.close();
    }

    // ---------------- REOPEN LOGIC ----------------
    public static void reopenTillCaseMaterial(int caseIndex) throws InterruptedException {
        // GO BACK TO HOME → WATCHES → BRAND → SAME PRODUCT → SAME CASE SIZE
        driver.findElement(By.xpath(locator.getProperty("watches"))).click();
        Thread.sleep(300);

        driver.findElement(By.xpath(elements.get(inputValue))).click();
        Thread.sleep(300);

        driver.findElements(By.xpath(locator.getProperty("selectOption"))).get(productCounter).click();
        Thread.sleep(300);

        driver.findElements(By.xpath(locator.getProperty("caseSize"))).get(caseIndex).click();
        Thread.sleep(300);
    }

    // ---------------- RESET AFTER PRODUCT COMPLETE ----------------
    public static void resetAllAfterProduct() throws IOException, InterruptedException {

        System.out.println("\n✅ Completed Product: " + productName);

        productCounter++; // Move to next product only now

        // Reset loop counters
        totalCounter = 0;
        totalCombinations = 0;
        caseSizeCounter = 0;
        caseMaterialCounter = 0;

        // Go back to HOME → WATCHES → BRAND → NEXT PRODUCT
        driver.findElement(By.xpath(locator.getProperty("watches"))).click();
        Thread.sleep(300);

        driver.findElement(By.xpath(elements.get(inputValue))).click();
        Thread.sleep(300);

        tempProductLoop();

        if (driver != null) {
            caseSizeStartLoop();
        }
    }

    private static void productResetAndNext() throws IOException, InterruptedException {
        driver.findElement(By.xpath(locator.getProperty("cancel"))).click();
        productCounter++;
        tempProductLoop();
        if (driver != null) caseSizeStartLoop();
    }
}
