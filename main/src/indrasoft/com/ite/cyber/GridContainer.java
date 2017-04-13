package indrasoft.com.ite.cyber;

import java.net.URL;

public class GridContainer implements Container {

	private String ipAddress;

	private String port;

	private URL url;
	
	private String id;
	
	public GridContainer() {
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getPort() {
		return port;
	}

	public URL getUrl() {
		return url;
	}

	@Override
	public ContainerType getContainerType() {
		return ContainerType.MainGrid;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

}
