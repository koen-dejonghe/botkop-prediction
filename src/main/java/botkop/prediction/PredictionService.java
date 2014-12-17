package botkop.prediction;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.bytecode.opencsv.CSVReader;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonString;
import com.google.api.client.util.Charsets;
import com.google.api.client.util.Key;
import com.google.api.services.prediction.Prediction;
import com.google.api.services.prediction.Prediction.Trainedmodels.Get;
import com.google.api.services.prediction.model.Input;
import com.google.api.services.prediction.model.Input.InputInput;
import com.google.api.services.prediction.model.Output;
import com.google.common.io.Closeables;

public class PredictionService {

	public static final String TYPE_CLASSIFICATION = "classification";

	public static final String TYPE_REGRESSION = "regression";

	@Autowired
	private Prediction prediction;

	@Autowired
	private JsonFactory jsonFactory;

	@Autowired
	private String projectId;
	
	@Autowired
	private String modelId;

	@Autowired
	private String modelType;

	public Get get() throws IOException{
		Get model = prediction.trainedmodels().get(projectId, modelId);
		return model;
	}

	public Output predict(Input input) throws IOException {

		if (TYPE_REGRESSION.equals(modelType)) {
			return predictRegression(input);
		}

		if (TYPE_CLASSIFICATION.equals(modelType)) {
			return predictClassification(input);
		}

		// TODO: throw appropriate runtime exception
		return null;
	}

	private Output predictClassification(Input input) throws IOException {
		Output output = prediction.trainedmodels()
				.predict(projectId, modelId, input).execute();
		return output;
	}

	private Output predictRegression(Input input) throws IOException {
		InputStream is = prediction.trainedmodels()
				.predict(projectId, modelId, input).executeAsInputStream();
		try {
			DoubleOutput out = jsonFactory.fromInputStream(is, Charsets.UTF_8,
					DoubleOutput.class);
			Output o = new Output();
			o.setOutputValue(out.outputValue);
			return o;
		} finally {
			Closeables.closeQuietly(is);
		}
	}

	public static class DoubleOutput {
		@Key
		@JsonString
		public java.lang.Double outputValue;
	}

	public Output predict(List<Object> csvInstance) throws IOException {
		Input input = new Input();
		InputInput inputInput = new InputInput();
		inputInput.setCsvInstance(csvInstance);
		input.setInput(inputInput);

		return predict(input);
	}

	public List<Output> predictFile(String csvFileName) throws IOException {
		ArrayList<Output> outputList = new ArrayList<Output>();

		CSVReader reader = new CSVReader(new FileReader(csvFileName));
		try {
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {

				ArrayList<Object> csvInstance = new ArrayList<Object>();
				for (String token : nextLine) {
					if (NumberUtils.isNumber(token)) {
						csvInstance.add(Double.parseDouble(token));
					} else {
						csvInstance.add(token);
					}
				}

				Output output = predict(csvInstance);
				outputList.add(output);
			}
		} finally {
			reader.close();
		}
		return outputList;
	}

}
