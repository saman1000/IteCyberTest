package indrasoft.com.ite.cyber;

import java.util.List;

public interface SeleniumDirector extends AutoCloseable {
	
	public void buildContainers();
	
	public List<Container> getNodes();

}