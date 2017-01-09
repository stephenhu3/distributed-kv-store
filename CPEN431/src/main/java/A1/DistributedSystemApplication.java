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
        /*
        Reply message format: the first 16 bytes are the unique ID of the corresponding request,
        the rest is the application level payload. Maximum payload size 16KB.

        Sample client output

        matei@lvs1$ java -jar A1.jar 137.82.252.191 5627 1381632

        Sending ID: 1381632
        Secret code length: 16
        Secret: D502F4661C49849B2B2FA95A623294BB
        */

    }

}
