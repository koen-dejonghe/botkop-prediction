package botkop.prediction;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import au.com.bytecode.opencsv.CSVReader;

import com.google.api.services.prediction.Prediction;
import com.google.api.services.prediction.model.Input;
import com.google.api.services.prediction.model.Input.InputInput;
import com.google.api.services.prediction.model.Output;

public class PredictionService {

	private Prediction prediction;

	private String projectId;

	private String modelId;

	public PredictionService(Prediction prediction, String projectId,
			String modelId) {
		this.prediction = prediction;
		this.projectId = projectId;
		this.modelId = modelId;
	}

	public Output predict(Input input) throws IOException {
		Output output = prediction.trainedmodels()
				.predict(projectId, modelId, input).execute();
		return output;
	}

	public Output predict(List<Object> csvInstance) throws IOException {
		Input input = new Input();
		InputInput inputInput = new InputInput();
		inputInput.setCsvInstance(csvInstance);
		input.setInput(inputInput);

		return predict(input);
	}

	public List<Output> predict(String csvFileName) throws IOException {

		ArrayList<Output> outputList = new ArrayList<Output>();

		CSVReader reader = new CSVReader(new FileReader(csvFileName));
		try {
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {				
				ArrayList<Object> csvInstance = new ArrayList<Object>();
				for (String token : nextLine){
					if (NumberUtils.isNumber(token)) {
						csvInstance.add(Double.parseDouble(token));
					}
					else {
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
