package indrasoft.com.ite.cyber;

import java.net.URL;

public interface Container {
	
	public ContainerType getContainerType();

	public void setIpAddress(String ipAddress);
	public String getIpAddress();
	
	public void setPort(String port);
	public String getPort();
	
	public void setUrl(URL url);
	public URL getUrl();
	
	public void setId(String id);
	public String getId();
	
}
