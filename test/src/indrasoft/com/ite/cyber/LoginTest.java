package indrasoft.com.ite.cyber;

import java.time.Duration;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import indrasoft.com.ite.config.Configuration;

public class LoginTest {

	private void login(WebDriver driver, String username, String password) throws Exception {
		WebElement loginMenu = new WebDriverWait(driver, 10).until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='js_navigation']//a[contains(@href,'login')]")));
		loginMenu.click();

		// wait max. 10 seconds until username label appears
		WebElement usernameLabel = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("username-lbl")));
		Assert.assertTrue(usernameLabel.isDisplayed());

		WebElement usernameBox = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
		usernameBox.sendKeys(username);
		WebElement passwordBox = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
		passwordBox.sendKeys(password);

		WebElement loginSubmitButton = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[@type='submit']")));
		loginSubmitButton.click();

		// wait until logged in
		(new WebDriverWait(driver, 10)).until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='js_navigation']//a[contains(@href,'logout')]")));
	}

	private void logout(WebDriver driver) throws Exception {
		WebElement logoutMenu = (new WebDriverWait(driver, 10)).until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='js_navigation']//a[contains(@href,'logout')]")));
		logoutMenu.click();

		Thread.sleep(100);
		Assert.assertThat(driver.getCurrentUrl(), CoreMatchers.containsString("index.php"));
		// wait until logged out
		(new WebDriverWait(driver, 10)).until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//*[@id='js_navigation']//a[contains(@href,'login')]")));
	}

	private void checkHomePageArticles(WebDriver driver, Integer expectedNumberOfArticles) throws Exception {
		// find the home page articles
		List<WebElement> homePageArticles = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("newsflash-title")));
		Assert.assertEquals("Not enough articles on home page", new Integer(homePageArticles.size()),
				expectedNumberOfArticles);
	}

	private void checkMainMenus(WebDriver driver, String[] expecteds) throws Exception {
		By mainMenuLocator = By.xpath("//*[@id='js_navigation']");
		By mainMenuOptionsLocator = By.xpath("//ul[@class='nav menu']/li");
		List<WebElement> menuOptions = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfNestedElementsLocatedBy(mainMenuLocator, mainMenuOptionsLocator));

		// find text of all main menu options
		String oneText = null;
		int counter = 0;
		for (WebElement oneOption : menuOptions) {
			oneText = oneOption.getText();
			Assert.assertEquals(expecteds[counter++], oneText);
		}
	}

	private void checkAllSelectableMenus(WebDriver driver, String[] expecteds) throws Exception {
		// By mainMenuLocator = By.xpath("//*[@id='js_navigation']");
		// By mainMenuOptionsLocator = By.xpath("//*/a");
		// List<WebElement> menuOptions = (new WebDriverWait(driver, 10))
		// .until(ExpectedConditions.presenceOfNestedElementsLocatedBy(mainMenuLocator,
		// mainMenuOptionsLocator));

		List<WebElement> menuOptions = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@id='js_navigation']//li/a")));

		// find text of all selectable menu options
		String oneText = null;
		int counter = 0;
		for (WebElement oneOption : menuOptions) {
			oneText = oneOption.getText();
			if (oneText == null || oneText.trim().length() == 0) {
				oneText = oneOption.getAttribute("innerHTML");
			}
			Assert.assertEquals(expecteds[counter++], oneText);
		}
		System.out.println();
	}

	@Ignore
	@Test
	public void localFirefoxTest() {
		WebDriver driver = null;
		try {
			Configuration config = new Configuration();

			// System.setProperty("webdriver.gecko.driver",
			// "C:\\saman\\agile\\geckodriver.exe");
			System.setProperty("webdriver.gecko.driver", "/home/seluser/drivers/geckodriver");
			String url = "http://ubuntudev:9001";

			DesiredCapabilities firefoxCapabilities = DesiredCapabilities.firefox();

			driver = new FirefoxDriver(firefoxCapabilities);
			driver.get(url);

			checkHomePageArticles(driver, 4);
			checkMainMenus(driver, new String[] { "Home", "Report Threat", "About", "Login" });

			login(driver, config.getEncryptedValue("approvedUser"), config.getEncryptedValue("approvedPwd"));
			// one of the articles should be displayed only to anonymous users
			checkHomePageArticles(driver, 3);
			checkMainMenus(driver, new String[] { "Home", "Resources", "Sectors", "Report Threat", "About", "Logout" });
			checkAllSelectableMenus(driver,
					new String[] { "Home", "Resources", "Events Conferences", "NIST Framework", "Outreach",
							"Sample Hacks", "Sectors", "Freight", "Infrastructure", "Passenger Vehicles", "Transit",
							"Report Threat", "Conact Us", "FAQ", "Logout" });
			logout(driver);
			Thread.sleep(1000);

			login(driver, config.getEncryptedValue("authorizedUser"), config.getEncryptedValue("approvedPwd"));
			checkMainMenus(driver, new String[] { "Home", "Resources", "Sectors", "Report Threat", "About", "Logout" });
			checkAllSelectableMenus(driver,
					new String[] { "Home", "Resources", "Events Conferences", "NIST Framework", "Outreach",
							"Sample Hacks", "Sectors", "Awareness", "Threat List", "Freight", "Infrastructure",
							"Passenger Vehicles", "Regions", "Transit", "Report Threat", "Conact Us", "FAQ", "Requests",
							"Logout" });
			logout(driver);

			driver.close();
			driver.quit();
			driver = null;
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}

	}

	@Ignore
	@Test
	public void htmlTest() {
		HtmlUnitDriver driver = null;
		try {
			Configuration config = new Configuration();
			String url = "http://ite-cyber.indrasoft.net";

			DesiredCapabilities firefoxCapabilities = DesiredCapabilities.firefox();
			firefoxCapabilities.setBrowserName("htmlunit");

			driver = new HtmlUnitDriver(firefoxCapabilities);
			driver.setJavascriptEnabled(true);
			driver.get(url);
			checkHomePageArticles(driver, 4);
			checkMainMenus(driver, new String[] { "Home", "Report Threat", "About", "Login" });

			login(driver, config.getEncryptedValue("approvedUser"), config.getEncryptedValue("approvedPwd"));
			// one of the articles should be displayed only to anonymous users
			checkHomePageArticles(driver, 3);
			checkMainMenus(driver, new String[] { "Home", "Resources", "Sectors", "Report Threat", "About", "Logout" });

			logout(driver);
			Thread.sleep(1000);

			login(driver, config.getEncryptedValue("authorizedUser"), config.getEncryptedValue("approvedPwd"));
			checkMainMenus(driver, new String[] { "Home", "Resources", "Sectors", "Report Threat", "About", "Logout" });

			logout(driver);

			driver.close();
			driver.quit();
			driver = null;
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
	}

	@Test
	public void testContactUs() {
//		WebDriver driver = null;
//		try {
//			Configuration config = new Configuration();
//
//			 System.setProperty("webdriver.gecko.driver",
//			 "C:\\saman\\agile\\geckodriver.exe");
////			System.setProperty("webdriver.gecko.driver", "/home/seluser/drivers/geckodriver");
//			String url = "http://ite-cyber.indrasoft.net";
//
//			DesiredCapabilities firefoxCapabilities = DesiredCapabilities.firefox();
//
//			driver = new FirefoxDriver(firefoxCapabilities);
//			driver.navigate().to(url);
//			driver.get(url);
//			
//			openContactUsPage(driver);
//			
//			driver.close();
//			driver.quit();
//			driver = null;
//		} catch (Exception e) {
//			Assert.fail(e.getMessage());
//		} finally {
//			if (driver != null) {
//				driver.quit();
//			}
//		}

		try (SeleniumDirector director = new SeleniumDirectorImpl();) {
			Configuration config = new Configuration();
			director.buildContainers();
			Thread.sleep(6000);
			for (Container oneContainer : director.getNodes()) {
				testContactUsRequest(oneContainer, config);
			}
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	private void testContactUsRequest(Container oneContainer, Configuration config) {
		WebDriver driver = null;
		try {
			String url = "http://ite-cyber.indrasoft.net";

			DesiredCapabilities browserCapabilities = null;
			switch (oneContainer.getContainerType()) {
			case FirefoxNode:
				browserCapabilities = DesiredCapabilities.firefox();
				
				// there is an issue where actions doesn't work on firefox docker image
//				break;
				return;

			case ChromeNode:
				browserCapabilities = DesiredCapabilities.chrome();
				break;

			default:
				Assert.fail("unknown type, " + oneContainer.getContainerType());
				break;
			}

			driver = new RemoteWebDriver(oneContainer.getUrl(), browserCapabilities);

			driver.navigate().to(url);
			System.out.println(driver.getCurrentUrl());
			driver.get(url);
			driver.manage().window().maximize();
			driver.get(url);

			openContactUsPage(driver);
			
			driver.close();
			driver.quit();
			driver = null;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			Assert.fail(oneContainer.getContainerType().name() + ": " + e.getMessage());
		} finally {
			if (driver != null) {
				driver.close();
				driver.quit();
			}
		}
	}
	
	private void openContactUsPage(WebDriver driver) throws Exception {
		By mainMenuLocator = By.xpath("//*[@id='js_navigation']");
		By mainMenuOptionsLocator = By.xpath("//ul/li[descendant::span]");
		WebElement aboutMenu = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfNestedElementLocatedBy(mainMenuLocator, mainMenuOptionsLocator));

		Actions contactUsAction = new Actions(driver);
		contactUsAction.moveToElement(aboutMenu).pause(Duration.ofMillis(100)).build().perform();
		
		WebElement contactUsOption = new WebDriverWait(driver, 10).until(ExpectedConditions
				.presenceOfNestedElementLocatedBy(aboutMenu, By.xpath("//a[contains(.,'act')]")));
		contactUsOption.click();
		
		// wait max. 10 seconds until username label appears
		WebElement legend = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//legend")));
		Assert.assertTrue(legend.isDisplayed());
		
		WebElement nameBox = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("contact_us___name")));
		nameBox.sendKeys("junit test user");
		
		WebElement emailBox = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("contact_us___email")));
		emailBox.sendKeys("junittest@dummy.com");
		
		WebElement subjectBox = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("contact_us___subject")));
		subjectBox.sendKeys("junit test subject");
		
		WebElement messageBox = (new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("contact_us___message")));
		messageBox.sendKeys("junit test message");
		
		WebElement submitButton = new WebDriverWait(driver, 10).until(ExpectedConditions
				.presenceOfElementLocated(By.xpath("//button[@type='submit']")));
		submitButton.click();
		//should be redirected to home page
		this.checkHomePageArticles(driver, 4);
	}

	@Test
	public void testRolesAndMenus() {
		try (SeleniumDirector director = new SeleniumDirectorImpl();) {
			Configuration config = new Configuration();
			director.buildContainers();
			Thread.sleep(6000);
			for (Container oneContainer : director.getNodes()) {
				testBrowserRolesAndMenus(oneContainer, config);
			}
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	private void testBrowserRolesAndMenus(Container oneContainer, Configuration config) {
		WebDriver driver = null;
		try {
			String url = "http://ite-cyber.indrasoft.net";

			DesiredCapabilities browserCapabilities = null;
			switch (oneContainer.getContainerType()) {
			case FirefoxNode:
				browserCapabilities = DesiredCapabilities.firefox();
				break;

			case ChromeNode:
				browserCapabilities = DesiredCapabilities.chrome();
				break;

			default:
				Assert.fail("unknown type, " + oneContainer.getContainerType());
				break;
			}

			driver = new RemoteWebDriver(oneContainer.getUrl(), browserCapabilities);
			driver.navigate().to(url);
			System.out.println(driver.getCurrentUrl());
			driver.get(url);
			driver.get(url);

			checkHomePageArticles(driver, 4);
			checkMainMenus(driver, new String[] { "Home", "Report Threat", "About", "Login" });

			login(driver, config.getEncryptedValue("approvedUser"), config.getEncryptedValue("approvedPwd"));
			// one of the articles should be displayed only to anonymous users
			checkHomePageArticles(driver, 3);
			checkMainMenus(driver, new String[] { "Home", "Resources", "Sectors", "Report Threat", "About", "Logout" });
			logout(driver);
			Thread.sleep(1000);

			login(driver, config.getEncryptedValue("authorizedUser"), config.getEncryptedValue("authorizedPwd"));
			checkMainMenus(driver, new String[] { "Home", "Resources", "Sectors", "Report Threat", "About", "Logout" });
			checkAllSelectableMenus(driver,
					new String[] { "Home", "Resources", "Events Conferences", "NIST Framework", "Outreach",
							"Sample Hacks", "Sectors", "Awareness", "Threat List", "Freight", "Infrastructure",
							"Passenger Vehicles", "Regions", "Transit", "Report Threat", "Conact Us", "FAQ", "Requests",
							"Logout" });
			logout(driver);

			driver.close();
			driver.quit();
			driver = null;
		} catch (Exception e) {
			Assert.fail(oneContainer.getContainerType().name() + ": " + e.getMessage());
		} finally {
			if (driver != null) {
				driver.close();
				driver.quit();
			}
		}
	}

}
