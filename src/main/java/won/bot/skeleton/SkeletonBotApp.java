package won.bot.skeleton;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import won.bot.framework.bot.utils.BotUtils;
import won.bot.skeleton.persistence.JsonParser;

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
	
	@Bean
	public CommandLineRunner loadJsonData(@Value("${json.sportplace.import.url}") String url) {
		return args -> {
			new JsonParser(url).parseData();
		};
	}
}
