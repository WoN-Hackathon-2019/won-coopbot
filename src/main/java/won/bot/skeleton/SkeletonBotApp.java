package won.bot.skeleton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import won.bot.framework.bot.utils.BotUtils;
import won.bot.skeleton.cli.engine.CliEngine;
import won.bot.skeleton.cli.engine.Command;
import won.bot.skeleton.cli.engine.DefaultValue;
import won.bot.skeleton.cli.engine.Optional;

@SpringBootConfiguration
@PropertySource("classpath:application.properties")
@ImportResource("classpath:/spring/app/botApp.xml")
public class SkeletonBotApp {
    public static void main(String[] args) throws Exception {
        if (!BotUtils.isValidRunConfig()) {
            System.exit(1);
        }
        SpringApplication app = new SpringApplication(SkeletonBotApp.class);
        app.setWebEnvironment(false);
        app.run(args);
        // ConfigurableApplicationContext applicationContext = app.run(args);
        // Thread.sleep(5*60*1000);
        // app.exit(applicationContext);
    }


    /*public SkeletonBotApp() {
        CliEngine engine = new CliEngine();
        engine.add(new Object() {
            @Command("/ja")
            void ja() {
                System.out.println("ja");
            }

            @Command("/na")
            void na(String arg1) {
                System.out.println(arg1);
            }

            @Command("/tra")
            void tra(String arg1, @Optional int age) {
                System.out.println(arg1 + "/age:" + age);
            }

            @Command("/la")
            void la(String arg1, @DefaultValue("6") int age) {
                System.out.println(arg1 + "/age:" + age);
            }
        });
        engine.parse("/ja");
        engine.parse("/na hallo");
        engine.parse("/tra timi");
        engine.parse("/la struppi");
    }*/
}
