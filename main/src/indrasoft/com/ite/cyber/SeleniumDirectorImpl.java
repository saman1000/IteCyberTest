package indrasoft.com.ite.cyber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;

public class SeleniumDirectorImpl implements SeleniumDirector {

	private DockerClient dockerClient;

	private List<Container> nodeList;

	private Container gridContainer;

	public SeleniumDirectorImpl() {
		nodeList = Collections.<Container>emptyList();
	}

	@Override
	public List<Container> getNodes() {
		return this.nodeList;
	}

	public void buildContainers() {
		try {
			buildDockerClient();
			ContainerBuilder gridBuilder = new DockerGridBuilder(dockerClient);
			ContainerBuilder firefoxBuilder = new FirefoxNodeBuilder(dockerClient);

			gridContainer = gridBuilder.build();

			ArrayList<Container> nodeList = new ArrayList<>();
			Container firefoxContainer = firefoxBuilder.build();
			nodeList.add(firefoxContainer);

			this.nodeList = Collections.unmodifiableList(nodeList);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private void buildDockerClient() {
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("tcp://192.168.111.193:2375").withDockerTlsVerify(false).build();

		DockerCmdExecFactory dockerCmdExecFactory = new JerseyDockerCmdExecFactory();

		dockerClient = DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(dockerCmdExecFactory).build();

		Info dockerInfo = dockerClient.infoCmd().exec();
		System.out.println(dockerInfo.getDriver());
	}

	@Override
	public void close() throws Exception {
		if (dockerClient == null) {
			return;
		}

		try {
			haltOneContainer(gridContainer.getId());
			for (Container oneContainer : nodeList) {
				haltOneContainer(oneContainer.getId());
			}
		} finally {
			this.dockerClient.close();
		}
	}

	private void haltOneContainer(String id) throws InterruptedException {
		Thread.sleep(1000);
		dockerClient.stopContainerCmd(id).exec();
		Thread.sleep(1000);
		dockerClient.removeContainerCmd(id).exec();
	}

}
