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

public class OW_new_Laptops {


    public static Properties locator;
    public static Properties config;
    public static List<WebElement> product;
    public static List<WebElement> storageList;
    public static List<WebElement> memoryList;
    public static List<WebElement> processorList;
    public static WebDriver driver;
    public static BufferedWriter wr;
    public static String productName, processorName, storageName, memoryName, tradePrice;
    public static int processorSize = 0, storageSize = 0, productSize = 0, memorySize = 0, totalSize = 0, sno = 1, datafetched = 0;
    public static int memoryCounter = 0, storageCounter = 0, processorCounter = 0, productCounter = 0, totalCounter = 0;
    public static String fileName;

    public static void main(String[] args) throws IOException, InterruptedException {
//  load all resource bundles
        locator = new Properties();
        FileInputStream fis = new FileInputStream("src/locators.properties");
        locator.load(fis);
        config = new Properties();
        FileInputStream con = new FileInputStream("src/config.properties");
        config.load(con);


//  Webdriver Launch
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get(config.getProperty("URL"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
//        Thread.sleep(2000);


//product selection
        driver.findElement(By.xpath(locator.getProperty("Laptops"))).click();
//        Thread.sleep(2000);
        System.out.println("Enter number accordingly: \n1 for Macbook\n2 for Dell Laptops\n3 for HP Laptops\4 for Microsoft Laptops" +
                "\n5 for Acer Laptops\n6 for Asus Laptops\n7 for Lenovo Laptops\n8 for Razer Laptops\n9 for Samsung Laptops");
        Scanner input = new Scanner(System.in);
        int inputValue = input.nextInt();
        //  Mapping of elemnt
        Map<Integer, WebElement> elements = new HashMap<>();
        elements.put(1, driver.findElement(By.xpath(locator.getProperty("MacBook"))));
        elements.put(2, driver.findElement(By.xpath(locator.getProperty("DellLaptops"))));
        elements.put(3, driver.findElement(By.xpath(locator.getProperty("HpLaptops"))));
        elements.put(4, driver.findElement(By.xpath(locator.getProperty("microsoftLaptops"))));
        elements.put(5, driver.findElement(By.xpath(locator.getProperty("acerLaptops"))));
        elements.put(6, driver.findElement(By.xpath(locator.getProperty("asusLaptops"))));
        elements.put(7, driver.findElement(By.xpath(locator.getProperty("lenovoLaptops"))));
        elements.put(8, driver.findElement(By.xpath(locator.getProperty("razerLaptops"))));
        elements.put(9, driver.findElement(By.xpath(locator.getProperty("samsungLaptops"))));
        WebElement selected = elements.get(inputValue);
        fileName = selected.getText().trim().replaceAll(" ", "");
        selected.click();

        // Excel
        wr = new BufferedWriter(new FileWriter(fileName, false));
        wr.write("SNO,Product Name,Processor,Storage,RAM,Trade in value");
        wr.newLine();
        wr.close();

//            driver.findElement(By.xpath(locator.getProperty("MacBook"))).click();

        tempProductLoop();
        if (driver != null) {
            processorCountLoop();
        }
    }

    public static void tempProductLoop() throws IOException, InterruptedException {
        product = new ArrayList<>();
//        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        product = driver.findElements(By.xpath(locator.getProperty("productList")));
        productSize = product.size();
        if (productCounter < productSize) {
            System.out.println("S.no " + (productCounter + 1) + " PRODUCT NAME: " + product.get(productCounter).getText());
            productName = product.get(productCounter).getText();          //product name stored
            Thread.sleep(300);
            driver.findElements(By.xpath(locator.getProperty("selectOption"))).get(productCounter).click();
            //To check if product have content or not
            List<WebElement> contentCheck = new ArrayList<>();
            try {
                contentCheck = driver.findElements(By.xpath(locator.getProperty("selectProcessor")));
//                Thread.sleep(200);
            } catch (Exception e) {
                System.out.println("Exception: " + e);
            }
            if (contentCheck.size() == 0) {
                wr = new BufferedWriter(new FileWriter(fileName, true));
                wr.write(sno + "," + productName + "," + "Processor-NO DATA,Storage-NO DATA,RAM-NO DATA,Trade in value-NO DATA");
                wr.newLine();
                wr.close();
                sno++;
                productCounter++;
//  set all other loop counter to zero
                processorSize = 0;
                storageSize = 0;
                productSize = 0;
                memorySize = 0;
                totalSize = 0;
                memoryCounter = 0;
                storageCounter = 0;
                processorCounter = 0;
                totalCounter = 0;
                System.out.println("\nProduct " + productName + " found empty========\n");
                driver.findElement(By.xpath(locator.getProperty("cancel"))).click();
                tempProductLoop();
//                processorCountLoop();
            } // if content check ends
            else {
                totalSizeCalc();
            }
        } else {
            System.out.println("All product under given Category>>Brand are covered." + "\n Total product searched: " + productCounter);
            driver.quit();
            driver = null;   // mark it as unusable
            return;
        }
    }

    private static void totalSizeCalc() throws InterruptedException {
        processorList = new ArrayList<>();
        processorList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
        for (int a = 0; a < processorList.size(); a++) {
            Thread.sleep(300);
            processorList.get(a).click();
            storageList = new ArrayList<>();
            storageList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
            for (int b = processorList.size(); b < storageList.size(); b++) {
                Thread.sleep(500);
                storageList.get(b).click();
                memoryList = new ArrayList<>();
                memoryList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
                totalSize = totalSize + (memoryList.size() - storageList.size());
            }
        }
        System.out.println("totalSizeCalc size=" + totalSize);
        driver.findElement(By.xpath(locator.getProperty("cancel"))).click();
//        Thread.sleep(500);
        driver.findElements(By.xpath(locator.getProperty("selectOption"))).get(productCounter).click();
    }

    public static void processorCountLoop() throws InterruptedException, IOException {

        processorList = new ArrayList<>();
        Thread.sleep(200);
//        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        processorList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
        processorSize = processorList.size();
        for (int j = 0; j < processorSize; j++) {
            processorCounter++;
            if (processorCounter <= processorSize) {
                processorList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
                System.out.println("-Processor " + (j + 1) + ". " + processorList.get(j).getText());
                processorName = processorList.get(j).getText();     //stored processor name to string
                processorList.get(j).click(); //till now user clicked on processor name and then storage fields visible to user
//                Thread.sleep(100);
            } else {
                System.out.println("---------------------------------------------------");
            }
            storageCountLoop();
        }
    }

    public static void storageCountLoop() throws InterruptedException, IOException {
        storageList = new ArrayList<>();
        Thread.sleep(200);
//        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        storageList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
        storageSize = storageList.size();
        storageCounter = processorSize;
        for (int k = processorSize; k < storageSize; k++) { //loop to read storage text
            storageCounter++;
            if (storageCounter <= storageSize) {
                storageList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
                System.out.println("---Storage " + (k + 1 - processorSize) + ". " + storageList.get(k).getText());
                storageName = storageList.get(k).getText();     //stored storage name to string
                storageList.get(k).click();//till now user clicked on storage name and now memory field is visible to user
//                Thread.sleep(100);
            } else {
                System.out.println("storageCount:" + storageSize + "\n" + "storageCounter:" + storageCounter + "\n");
                break;
            }
            memoryCountLoop();
        }
    }

    public static void memoryCountLoop() throws InterruptedException, IOException {
        memoryList = new ArrayList<>();
        Thread.sleep(200);
//        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        memoryList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
        memorySize = memoryList.size();
        memoryCounter = storageSize;

        for (int l = storageSize; l < memorySize; l++) {
            //loop for reading memory element text
            memoryCounter++;
            totalCounter++;
            System.out.println(", Total Counter - " + totalCounter + " / " + totalSize);
            if (memoryCounter <= memorySize) {
                wr = new BufferedWriter(new FileWriter(fileName, true));
                memoryList = driver.findElements(By.xpath(locator.getProperty("eachButton")));
//                Thread.sleep(300);
                memoryList.get(l).click();
                memoryName = memoryList.get(l).getText();     //stored Ram memory to string
                System.out.print("------Memory " + (l + 1 - storageSize) + ". " + memoryList.get(l).getText());
                driver.findElement(By.xpath(locator.getProperty("doneButton"))).click();
//                Thread.sleep(2000);

                yesAndPrintPrice();
            } else {
//   Else- reset the counter of memory and storage
                memoryCounter = 0;
                storageCounter = 0;
            }
            if (totalCounter >= totalSize) {
                System.out.println("totalCounter==totalSize");
                productCounter++;
                processorSize = 0;
                storageSize = 0;
                productSize = 0;
                memorySize = 0;
                totalSize = 0;
                memoryCounter = 0;
                storageCounter = 0;
                processorCounter = 0;
                totalCounter = 0;

                driver.findElement(By.xpath(locator.getProperty("cancel"))).click();
                tempProductLoop();
                if (driver != null) {
                    processorCountLoop();
                }
            }
        }
    }

    public static void yesAndPrintPrice() throws InterruptedException, IOException {
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        while (true) { // clicking Yes button until its available
            try {
                driver.findElement(By.xpath(locator.getProperty("yesButton"))).click();
//                Thread.sleep(500);
            } catch (Exception e) {
                if (e.getMessage().contains("Unable to locate element")) {
                    break;
                } else {
                    throw e;
                }
            }
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        String priceProduct = driver.findElement(By.xpath(locator.getProperty("priceText"))).getText();
        System.out.print(" :-PRICE:" + priceProduct);
        tradePrice = priceProduct;
        datafetched++;
        System.out.println("  ---Total Data Fetched=" + datafetched);

        writeAllData(); //writing to CSV file

        driver.navigate().refresh(); //to move back to product list
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(40));
        driver.findElements(By.xpath(locator.getProperty("selectOption"))).get(productCounter).click();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        reopenTillMemory();
    }

    private static void writeAllData() throws IOException {
        wr = new BufferedWriter(new FileWriter(fileName, true));
        String stringSno = Integer.toString(sno);
        wr.write(stringSno + "," + productName + "," + processorName + "," + storageName + "," + memoryName + "," + tradePrice);
        wr.newLine();
        sno++;
        wr.close();
    }

    public static void reopenTillMemory() throws InterruptedException, IOException {
        Thread.sleep(200);
        driver.findElements(By.xpath(locator.getProperty("eachButton"))).get(processorCounter - 1).click();
//        Thread.sleep(500);
        driver.findElements(By.xpath(locator.getProperty("eachButton"))).get(storageCounter - 1).click();

    }


}
