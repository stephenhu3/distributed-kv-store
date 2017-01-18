package A3;

import A3.cli.ProtocolBufferStudentNumberRequestCommand;
import A3.cli.RawBytesStudentNumberRequestCommand;
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
    }

    @Override
    public void run(final DistributedSystemConfiguration configuration,
                    final Environment environment) {
    }
}
