package indrasoft.com.ite.cyber;

import java.net.URL;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.Link;
import com.github.dockerjava.api.model.NetworkSettings;

public class ChromeNodeBuilder implements ContainerBuilder {
	
	private DockerClient dockerClient;

	public ChromeNodeBuilder(DockerClient dockerClient) {
		this.dockerClient = dockerClient;
	}

	private void buildNode(ChromeContainer chromeNode) throws Exception {
		
		String node_port = "5555";
		CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd(chromeNode.getContainerType().getdockerImage())
//				.withEnv("--detached=true") //, "NODE_PORT=" + node_port)
				.withLinks(new Link("selenium-hub", "hub")) //.withPublishAllPorts(Boolean.TRUE)
				.withName("node-chrome").exec();
		String[] warnings = createContainerResponse.getWarnings();
		if (warnings != null) {
			for (String oneWarning : createContainerResponse.getWarnings()) {
				System.err.println(oneWarning);
			}
		} else {
			chromeNode.setId(createContainerResponse.getId());
			dockerClient.startContainerCmd(chromeNode.getId()).exec();

			chromeNode.setPort(node_port);
			
			InspectContainerResponse inspectContainerResponse = dockerClient
					.inspectContainerCmd(chromeNode.getId()).exec();
			NetworkSettings networkSettings = inspectContainerResponse.getNetworkSettings();
			ContainerNetwork containerNetwork = networkSettings.getNetworks().get("bridge");
			
			chromeNode.setIpAddress(containerNetwork.getIpAddress());
			
			StringBuilder chromeNodeUrl = new StringBuilder("http://");
			chromeNodeUrl.append(chromeNode.getIpAddress());
			chromeNodeUrl.append(":");
			chromeNodeUrl.append(node_port);
			chromeNodeUrl.append("/wd/hub");
			chromeNode.setUrl(new URL(chromeNodeUrl.toString()));
		}
	}

	@Override
	public Container build() throws Exception {
		ChromeContainer chromeNode = new ChromeContainer();

		buildNode(chromeNode);
		
		return chromeNode;
	}

}
