package indrasoft.com.ite.cyber;

import java.net.URL;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.Link;
import com.github.dockerjava.api.model.NetworkSettings;

public class FirefoxNodeBuilder implements ContainerBuilder {
	
	private DockerClient dockerClient;

	public FirefoxNodeBuilder(DockerClient dockerClient) {
		this.dockerClient = dockerClient;
	}

	private void buildNode(FirefoxContainer firefoxNode) throws Exception {
		
		String node_port = "9080";
		CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd(firefoxNode.getContainerType().getdockerImage())
				.withEnv("--detached=true", "NODE_PORT=" + node_port)
				.withLinks(new Link("selenium-hub", "hub")).withPublishAllPorts(Boolean.TRUE)
				.withName("node-firefox").exec();
		String[] warnings = createContainerResponse.getWarnings();
		if (warnings != null) {
			for (String oneWarning : createContainerResponse.getWarnings()) {
				System.err.println(oneWarning);
			}
		} else {
			firefoxNode.setId(createContainerResponse.getId());
			dockerClient.startContainerCmd(firefoxNode.getId()).exec();

			firefoxNode.setPort(node_port);
			
			InspectContainerResponse inspectContainerResponse = dockerClient
					.inspectContainerCmd(firefoxNode.getId()).exec();
			NetworkSettings networkSettings = inspectContainerResponse.getNetworkSettings();
			ContainerNetwork containerNetwork = networkSettings.getNetworks().get("bridge");
			
			firefoxNode.setIpAddress(containerNetwork.getIpAddress());
			
			StringBuilder firefoxNodeUrl = new StringBuilder("http://");
			firefoxNodeUrl.append(firefoxNode.getIpAddress());
			firefoxNodeUrl.append(":");
			firefoxNodeUrl.append(node_port);
			firefoxNodeUrl.append("/wd/hub");
			firefoxNode.setUrl(new URL(firefoxNodeUrl.toString()));
		}
	}

	@Override
	public Container build() throws Exception {
		FirefoxContainer firefoxNode = new FirefoxContainer();

		buildNode(firefoxNode);
		
		return firefoxNode;
	}

}
