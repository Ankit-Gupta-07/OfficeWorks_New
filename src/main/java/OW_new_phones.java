import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class OW_new_phones {

    public static Properties locator;
    public static Properties config;
    public static Map<Integer, String> elements;
    public static List<WebElement> product;
    public static List<WebElement> storageList;
    public static WebDriver driver;
    public static BufferedWriter wr;
    public static String productName, storageName, tradePrice;
    public static int storageSize = 0, productSize = 0, sno = 1, datafetched = 0;
    public static int productCounter = 0, storageCounter = 0, totalCounter = 0, totalCombinations = 0;
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

        driver.findElement(By.xpath(locator.getProperty("phones"))).click();
        System.out.println("Enter number accordingly: \n1 for iPhones\n2 for Samsung\n3 for Google\n4 for Other phones");

        inputValue = new Scanner(System.in).nextInt();

        elements = new HashMap<>();
        elements.put(1, locator.getProperty("iphones"));
        elements.put(2, locator.getProperty("samsungPhones"));
        elements.put(3, locator.getProperty("googlePhones"));
        elements.put(4, locator.getProperty("otherPhones"));

        WebElement selected = driver.findElement(By.xpath(elements.get(inputValue)));
        fileName = selected.getText().trim().replaceAll(" ", "") + ".csv";
        selected.click();

        wr = new BufferedWriter(new FileWriter(fileName, false));
        wr.write("SNO,Product Name,Storage,Trade in Value");
        wr.newLine();
        wr.close();

        tempProductLoop();
        if (driver != null) storageCountLoop();
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

        storageList = driver.findElements(By.xpath(locator.getProperty("eachButtonPhones")));
        if (storageList.size() == 0) {
            saveNoData();
            moveToNextProduct();
        } else {
            totalCombinations = storageList.size();
            System.out.println("Total Variations: " + totalCombinations);
        }
    }

    // ---------------- STORAGE LOOP ----------------
    public static void storageCountLoop() throws InterruptedException, IOException {
        storageList = driver.findElements(By.xpath(locator.getProperty("eachButtonPhones")));
        storageSize = storageList.size();

        for (int k = 0; k < storageSize; k++) {
            storageList = driver.findElements(By.xpath(locator.getProperty("eachButtonPhones")));
            storageName = storageList.get(k).getText();
            System.out.println("   ├── Storage: " + storageName);

            storageList.get(k).click();
            Thread.sleep(100);

            driver.findElement(By.xpath(locator.getProperty("doneButton"))).click();
            yesAndPrintPrice();

            totalCounter++;
            System.out.println("Progress: " + totalCounter + " / " + totalCombinations);

            if (totalCounter >= totalCombinations) {
                resetAllAfterProduct();
                return;
            }

            // Reopen same product again for next storage option
            reopenTillStorage();
        }
    }

    // ---------------- PRICE FETCH ----------------
    public static void yesAndPrintPrice() throws InterruptedException, IOException {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));

        while (true) {
            try {
                WebElement yesBtn = driver.findElement(By.xpath(locator.getProperty("yesButton")));
                yesBtn.click();
                Thread.sleep(200);
            } catch (Exception e) {
                break;
            }
        }

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement priceElement = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath(locator.getProperty("priceText"))
                    )
            );

            String price = priceElement.getText();
            System.out.println("   💰 PRICE: " + price);
            tradePrice = price;
            datafetched++;
            writeAllData();

        } catch (Exception e) {
            System.out.println("❌ Price not found for: " + productName + " - " + storageName);
            wr = new BufferedWriter(new FileWriter(fileName, true));
            wr.write(sno + "," + productName + "," + storageName + ",Price Not Found");
            wr.newLine();
            wr.close();
        }

        // After fetching price, click cancel (goes back to home)
        driver.findElement(By.xpath(locator.getProperty("cancel"))).click();
        Thread.sleep(400);
    }

    // ---------------- SAVE TO CSV ----------------
    private static void writeAllData() throws IOException {
        wr = new BufferedWriter(new FileWriter(fileName, true));
        wr.write(sno + "," + productName + "," + storageName + "," + tradePrice);
        wr.newLine();
        wr.close();
        sno++;
    }

    private static void saveNoData() throws IOException {
        wr = new BufferedWriter(new FileWriter(fileName, true));
        wr.write(sno + "," + productName + ",NO DATA,NO DATA");
        wr.newLine();
        wr.close();
        sno++;
    }

    // ---------------- REOPEN LOGIC (like watches) ----------------
    public static void reopenTillStorage() throws InterruptedException {
        // HOME → PHONES → BRAND → SAME PRODUCT

        driver.findElement(By.xpath(locator.getProperty("phones"))).click();
        Thread.sleep(400);

        driver.findElement(By.xpath(elements.get(inputValue))).click();
        Thread.sleep(400);

        // Wait for product list to appear
        List<WebElement> products = new ArrayList<>();
        int retries = 0;

        while (products.size() == 0 && retries < 10) {
            Thread.sleep(500);
            products = driver.findElements(By.xpath(locator.getProperty("selectOption")));
            retries++;
        }

        if (products.size() == 0) {
            System.out.println("⚠️ No products found after reopen. Retrying navigation...");
            driver.navigate().refresh();
            Thread.sleep(1000);
            reopenTillStorage(); // retry once more
            return;
        }

        // Ensure index is valid before clicking
        if (productCounter < products.size()) {
            products.get(productCounter).click();
            Thread.sleep(400);
        } else {
            System.out.println("⚠️ Product index out of range. Counter=" + productCounter + ", Available=" + products.size());
            driver.navigate().refresh();
            Thread.sleep(1000);
            reopenTillStorage();
        }
    }


    // ---------------- AFTER PRODUCT COMPLETE ----------------
    public static void resetAllAfterProduct() throws IOException, InterruptedException {
        System.out.println("\n✅ Completed Product: " + productName);

        productCounter++;
        totalCounter = 0;
        totalCombinations = 0;
        storageCounter = 0;

        driver.findElement(By.xpath(locator.getProperty("phones"))).click();
        Thread.sleep(300);

        driver.findElement(By.xpath(elements.get(inputValue))).click();
        Thread.sleep(300);

        tempProductLoop();

        if (driver != null) storageCountLoop();
    }

    private static void moveToNextProduct() throws IOException, InterruptedException {
        driver.findElement(By.xpath(locator.getProperty("cancel"))).click();
        productCounter++;
        tempProductLoop();
        if (driver != null) storageCountLoop();
    }
}
