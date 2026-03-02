package ai.nvwa.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.shell.command.annotation.CommandScan;

@SpringBootApplication(scanBasePackages={"ai.nvwa"})
@EnableAspectJAutoProxy(exposeProxy = true)
@CommandScan
public class NvwaApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(NvwaApplication.class);
//        app.setBannerMode(Banner.Mode.OFF); // 可选：关闭Banner
        app.run(args);
    }

}


