package live.qsmc.api;

import live.qsmc.core2.Quipt;
import live.qsmc.core2.QuiptIntegration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class QuiptApiApplication extends QuiptIntegration {

    private static QuiptIntegration quipt;

    public static void main(String[] args)
    {
        SpringApplication.run(QuiptApiApplication.class, args);
        quipt = new QuiptApiApplication();
        Quipt.INSTANCE.enable(quipt);
    }

    public static QuiptIntegration get() {
        return quipt;
    }

    @Override
    public String name() {
        return "API";
    }

    @Override
    public String version() {
        return "1";
    }

    @Override
    public File folder() {
        return new File("quipt/api");
    }

    @Override
    public void enable() {
    }
}
