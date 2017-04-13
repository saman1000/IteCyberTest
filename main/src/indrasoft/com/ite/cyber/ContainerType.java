package indrasoft.com.ite.cyber;

public enum ContainerType {
	
	MainGrid("selenium/hub"), FirefoxNode("selenium/node-firefox"), ChromeNode("selenium/node-chrome");
	
	private String dockerImage;

	private ContainerType(String dockerImage) {
		this.dockerImage = dockerImage;
	}
	
	public String getdockerImage() {
		return this.dockerImage;
	}
}
