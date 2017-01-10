package A1;

import A1.cli.UDPRequestCommand;
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
        bootstrap.addCommand(new UDPRequestCommand());
    }

    @Override
    public void run(final DistributedSystemConfiguration configuration,
                    final Environment environment) {
    }
}
