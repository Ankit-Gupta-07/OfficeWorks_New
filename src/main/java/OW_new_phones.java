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
    public static List<WebElement> product;
    public static List<WebElement> storageList;
    public static WebDriver driver;
    public static BufferedWriter wr;
    public static String productName, storageName, tradePrice;
    public static int storageSize = 0, productSize = 0, sno = 1, datafetched = 0;
    public static int productCounter = 0;
    public static String fileName;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Load resource bundles
        locator = new Properties();
        FileInputStream fis = new FileInputStream("src/locators.properties");
        locator.load(fis);

        config = new Properties();
        FileInputStream con = new FileInputStream("src/config.properties");
        config.load(con);

        // WebDriver launch
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get(config.getProperty("URL"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

        // Product selection
        driver.findElement(By.xpath(locator.getProperty("phones"))).click();
        System.out.println("Enter number accordingly: \n1 for iPhones\n2 for Samsung\n3 for Google\n4 for Other phones");

        Scanner input = new Scanner(System.in);
        int inputValue = input.nextInt();

        // Mapping of elements
        Map<Integer, WebElement> elements = new HashMap<>();
        elements.put(1, driver.findElement(By.xpath(locator.getProperty("iphones"))));
        elements.put(2, driver.findElement(By.xpath(locator.getProperty("samsungPhones"))));
        elements.put(3, driver.findElement(By.xpath(locator.getProperty("googlePhones"))));
        elements.put(4, driver.findElement(By.xpath(locator.getProperty("otherPhones"))));

        WebElement selected = elements.get(inputValue);
        fileName = selected.getText().trim().replaceAll(" ", "") + ".csv";
        selected.click();

        // Create CSV header
        wr = new BufferedWriter(new FileWriter(fileName, false));
        wr.write("SNO,Product Name,Storage,Trade in Value");
        wr.newLine();
        wr.close();

        // Start main loops
        tempProductLoop();
    }

    public static void tempProductLoop() throws IOException, InterruptedException {
        product = driver.findElements(By.xpath(locator.getProperty("productList")));
        productSize = product.size();

        if (productCounter < productSize) {
            productName = product.get(productCounter).getText();
            System.out.println("📱 Product " + (productCounter + 1) + ": " + productName);

            driver.findElements(By.xpath(locator.getProperty("selectOption"))).get(productCounter).click();
            Thread.sleep(500);

            // Check if product has storage options
            storageList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
            if (storageList.size() == 0) {
                System.out.println("❌ No storage options for: " + productName);
                wr = new BufferedWriter(new FileWriter(fileName, true));
                wr.write(sno + "," + productName + ",No Storage Options,NA");
                wr.newLine();
                wr.close();
                sno++;
                productCounter++;
                driver.findElement(By.xpath(locator.getProperty("cancel"))).click();
                tempProductLoop();
            } else {
                storageCountLoop();
            }
        } else {
            System.out.println("✅ All products fetched successfully!");
            driver.quit();
        }
    }

    public static void storageCountLoop() throws InterruptedException, IOException {
        storageList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
        storageSize = storageList.size();

        for (int k = 0; k < storageSize; k++) {
            storageList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
            storageName = storageList.get(k).getText();
            System.out.println("   ├── Storage: " + storageName);
            storageList.get(k).click();
            Thread.sleep(50);
            driver.findElement(By.xpath(locator.getProperty("doneButton"))).click();
            yesAndPrintPrice();
        }

        productCounter++;
        driver.findElement(By.xpath(locator.getProperty("cancel"))).click();
        tempProductLoop(); // Move to next phone
    }

    public static void yesAndPrintPrice() throws InterruptedException, IOException {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));

        // 1️⃣ Click all YES buttons until price screen appears
        while (true) {
            try {
                WebElement yesBtn = driver.findElement(By.xpath(locator.getProperty("yesButton")));
                yesBtn.click();
                Thread.sleep(200); // small pause for next question to load
            } catch (Exception e) {
                // No more YES buttons present
                break;
            }
        }

        // 2️⃣ Wait for price text to appear after all YES buttons handled
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement priceElement = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath(locator.getProperty("priceText"))
                    )
            );

            String price = priceElement.getText();
            System.out.println("   >> PRICE: " + price);
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

        // 3️⃣ Refresh page and re-open current product for next storage option
        driver.navigate().refresh();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.findElements(By.xpath(locator.getProperty("selectOption"))).get(productCounter).click();
    }


    private static void writeAllData() throws IOException {
        wr = new BufferedWriter(new FileWriter(fileName, true));
        wr.write(sno + "," + productName + "," + storageName + "," + tradePrice);
        wr.newLine();
        wr.close();
        sno++;
    }
}
