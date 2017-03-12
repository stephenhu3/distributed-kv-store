package A7;

import A7.cli.ProtocolBufferKeyValueStoreRequestCommand;
import A7.cli.ProtocolBufferStudentNumberRequestCommand;
import A7.cli.RawBytesStudentNumberRequestCommand;
import A7.cli.UDPServerThreadSpawnCommand;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class DistributedSystemApplication extends Application<DistributedSystemConfiguration> {
    public static void main(final String[] args) throws Exception {
        new DistributedSystemApplication().run(args);
    }

    @Override
    public String getName() {
        return "DistributedSystem";
    }

    @Override
    public void initialize(final Bootstrap<DistributedSystemConfiguration> bootstrap) {
        bootstrap.addCommand(new RawBytesStudentNumberRequestCommand());
        bootstrap.addCommand(new ProtocolBufferStudentNumberRequestCommand());
        bootstrap.addCommand(new ProtocolBufferKeyValueStoreRequestCommand());
        bootstrap.addCommand(new UDPServerThreadSpawnCommand());
    }

    @Override
    public void run(final DistributedSystemConfiguration configuration,
                    final Environment environment) {
    }
}
