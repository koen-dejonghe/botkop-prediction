package botkop.prediction.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import botkop.prediction.PredictionService;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow.Builder;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.prediction.Prediction;
import com.google.api.services.prediction.PredictionScopes;

@Configuration
public class CoreConfig {

	private static final Logger L = LoggerFactory.getLogger(CoreConfig.class);

	@Value("${application.name}")
	private String applicationName;

	@Value("${project.id}")
	public String projectId;

	@Value("${model.id}")
	private String modelId;

	@Value("${storage.data.location}")
	private String storageDataLocation;

	@Value("${data.store.dir}")
	private String dataStoreDir;

	@Bean
	public File dataStoreDir() {
		return new java.io.File(System.getProperty("user.home"), dataStoreDir);
	}

	@Bean
	public FileDataStoreFactory dataStoreFactory() throws IOException {
		return new FileDataStoreFactory(dataStoreDir());
	}

	@Bean
	public HttpTransport httpTransport() throws GeneralSecurityException,
			IOException {
		return GoogleNetHttpTransport.newTrustedTransport();
	}

	@Bean
	public JsonFactory jsonFactory() {
		return JacksonFactory.getDefaultInstance();
	}

	@Bean
	public Credential credential() throws IOException, GeneralSecurityException {
		
		L.info("instantiating credentials");
		
		// load client secrets
		InputStreamReader reader = new InputStreamReader(
				CoreConfig.class.getResourceAsStream("/client_secrets.json"));

		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
				jsonFactory(), reader);		
		
		// set up authorization code flow
		Set<String> singleton = Collections
				.singleton(PredictionScopes.PREDICTION);
		Builder builder = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport(), jsonFactory(), clientSecrets, singleton);
		builder = builder.setDataStoreFactory(dataStoreFactory());

		AuthorizationCodeFlow flow = builder.build();

		// authorize
		return new AuthorizationCodeInstalledApp(flow,
				new LocalServerReceiver()).authorize("user");
	}

	@Bean
	public Prediction prediction() throws GeneralSecurityException,
			IOException {
		L.info("instantiating prediction");
		return new Prediction.Builder(httpTransport(), jsonFactory(),
				credential()).setApplicationName(applicationName).build();
	}
	
	@Bean 
	PredictionService predicitionService() throws GeneralSecurityException, IOException{
		L.info("instantiating prediction service");
		return new PredictionService(prediction(), projectId, modelId);
	}

}
