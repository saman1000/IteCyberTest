package indrasoft.com.ite.cyber;

public class MainSelenium {

	public static void main(String[] args) {
		try (SeleniumDirector director = new SeleniumDirectorImpl();) {
			director.buildContainers();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

}
