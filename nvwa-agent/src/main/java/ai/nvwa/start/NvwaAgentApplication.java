package ai.nvwa.start;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages={"ai.nvwa"})
@MapperScan({"ai.nvwa.tool.datastore.dal.mapper"})
@EnableAspectJAutoProxy(exposeProxy = true)
public class NvwaAgentApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(NvwaAgentApplication.class, args);
    }

}


