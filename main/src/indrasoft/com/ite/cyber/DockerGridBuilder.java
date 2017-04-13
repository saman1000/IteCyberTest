package indrasoft.com.ite.cyber;

import java.net.URL;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;

public class DockerGridBuilder implements ContainerBuilder {

	private DockerClient dockerClient;

	public DockerGridBuilder(DockerClient dockerClient) {
		this.dockerClient = dockerClient;
	}

	private void buildNode(GridContainer gridContainer) throws Exception {
		Ports portBindings = new Ports();
		Integer portNumber = 4444;
		ExposedPort tcp4444 = ExposedPort.tcp(portNumber.intValue());
		portBindings.bind(tcp4444, Binding.bindPort(portNumber.intValue()));
		CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd("selenium/hub")
				.withEnv("GRID_TIMEOUT=10", "--detahced=true").withPublishAllPorts(Boolean.TRUE)
				// .withAliases("selenium-hub")
				.withExposedPorts(tcp4444).withPortBindings(portBindings).withName("selenium-hub").exec();
		String[] warnings = createContainerResponse.getWarnings();
		if (warnings != null) {
			for (String oneWarning : createContainerResponse.getWarnings()) {
				System.err.println(oneWarning);
			}
		} else {
			gridContainer.setId(createContainerResponse.getId());
			
			dockerClient.startContainerCmd(gridContainer.getId()).exec();
			
			gridContainer.setPort(portNumber.toString());
			
			InspectContainerResponse inspectContainerResponse = dockerClient
					.inspectContainerCmd(gridContainer.getId()).exec();
			
			NetworkSettings networkSettings = inspectContainerResponse.getNetworkSettings();
			ContainerNetwork containerNetwork = networkSettings.getNetworks().get("bridge");
			
			gridContainer.setIpAddress(containerNetwork.getIpAddress());
			
			StringBuilder gridUrl = new StringBuilder("http://");
			gridUrl.append(gridContainer.getIpAddress());
			gridUrl.append(":");
			gridUrl.append(gridContainer.getPort());
			gridUrl.append("/grid/console");

			gridContainer.setUrl(new URL(gridUrl.toString()));
		}
	}

	@Override
	public Container build() throws Exception {
		GridContainer gridContainer = new GridContainer();

		buildNode(gridContainer);

		return gridContainer;
	}

}
