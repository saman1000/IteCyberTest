package indrasoft.com.ite.cyber;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Link;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;

public class IteDockerContainer {

	public IteDockerContainer() {

	}

	private void startFirefoxNode(DockerClient dockerClient) throws Exception {
		// Ports portBindings = new Ports();
		// ExposedPort tcp4444 = ExposedPort.tcp(4444);
		// Set<Link> allLinks = new HashSet<>();
		// Set<Link> links = dockerClient.listContainersCmd().exec().stream()
		// .filter(container -> container.getNames()[0].endsWith("hub"))
		// .map(container -> new Link(container.getNames()[0], "selenium-hub"))
		// .collect(Collectors.toSet());
		// allLinks.addAll(links);

		CreateContainerResponse createContainerResponse = dockerClient.createContainerCmd("selenium/node-firefox")
				.withEnv("--detached=true").withLinks(new Link("selenium-hub", "hub")).withPublishAllPorts(Boolean.TRUE)
				// .withExposedPorts(tcp4444)
				// .withPortBindings(portBindings)
				.withName("node-firefox").exec();
		String[] warnings = createContainerResponse.getWarnings();
		if (warnings != null) {
			for (String oneWarning : createContainerResponse.getWarnings()) {
				System.err.println(oneWarning);
			}
		} else {
			dockerClient.startContainerCmd(createContainerResponse.getId()).exec();

			InspectContainerResponse inspectContainerResponse = dockerClient
					.inspectContainerCmd(createContainerResponse.getId()).exec();
			NetworkSettings networkSettings = inspectContainerResponse.getNetworkSettings();
			networkSettings.getGlobalIPv6Address();
			inspectContainerResponse.getPath();

			Thread.sleep(1000);
			dockerClient.stopContainerCmd(createContainerResponse.getId()).exec();
			Thread.sleep(1000);
			dockerClient.removeContainerCmd(createContainerResponse.getId()).exec();
		}
	}

	public void startDockerServer() throws Exception {
		DockerClient dockerClient = null;
		try {
			// DockerClientConfig config =
			// DefaultDockerClientConfig.createDefaultConfigBuilder()
			// .withDockerHost("unix:///var/run/docker.sock").withDockerTlsVerify(false).build();

			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost("tcp://192.168.111.193:2375").withDockerTlsVerify(false).build();

			DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory(); // new
																							// NettyDockerCmdExecFactory();

			dockerClient = DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(dockerCmdExecFactory)
					.build();

			Info dockerInfo = dockerClient.infoCmd().exec();
			System.out.println(dockerInfo.getDriver());

			Ports portBindings = new Ports();
			ExposedPort tcp4444 = ExposedPort.tcp(4444);
			portBindings.bind(tcp4444, Binding.bindPort(4444));
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
				dockerClient.startContainerCmd(createContainerResponse.getId()).exec();
				InspectContainerResponse inspectContainerResponse = dockerClient
						.inspectContainerCmd(createContainerResponse.getId()).exec();
				System.out.println("Selenium hub: " + inspectContainerResponse.getCreated());

				startFirefoxNode(dockerClient);

				Thread.sleep(1000);
				dockerClient.stopContainerCmd(inspectContainerResponse.getId()).exec();
				Thread.sleep(1000);
				dockerClient.removeContainerCmd(inspectContainerResponse.getId()).exec();
			}
		} finally {
			if (dockerClient != null) {
				dockerClient.close();
			}
		}
	}

	public static void main(String[] args) {
		try {
			IteDockerContainer localContainer = new IteDockerContainer();
			localContainer.startDockerServer();
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		} finally {
			// System.exit(0);
		}

	}

}
