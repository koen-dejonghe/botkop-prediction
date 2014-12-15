package botkop.prediction;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.stereotype.Controller;

import com.google.api.services.prediction.model.Output;

@Controller
public class Predictor implements CommandLineRunner {

	private static final Logger L = LoggerFactory.getLogger(Predictor.class);

	@Autowired
	private PredictionService predictionService;

	private String sayHello() throws IOException {
		List<Object> input = Collections
				.<Object> singletonList("Es esta frase en Español?");
		Output output = predictionService.predict(input);
		return output.getOutputLabel();
	}
	
	private void predictFromFile(String fname) throws IOException{		
		List<Output> output = predictionService.predictFile(fname); 
		for(Output o : output) {
			// System.out.println(o.getOutputLabel());
			System.out.println(o.toPrettyString());
		}
	}

	@Override
	public void run(String... args) throws Exception {

		PropertySource<?> ps = new SimpleCommandLinePropertySource(args);
		
		if (ps.containsProperty("sayHello")) {
			String s = sayHello();
			L.info(s);		
		}
		
		if (ps.containsProperty("predict") && ps.containsProperty("file-name")) {
			predictFromFile(ps.getProperty("file-name").toString());
		}
		
		

	}
}