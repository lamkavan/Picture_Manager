package backend;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class connects to the Clarifai Image Recognition API and classifies images.
 * <p>
 * This class depends on 4 jar files:
 * core-2.3.0.jar
 * gson-2.7.jar
 * okhttp-3.4.1.jar
 * okio-1.9.0.jar
 * <p>
 * The jar files were downloaded here:
 * https://jar-download.com/explore-java-source-code.php?a=core&g=com.clarifai.clarifai-api2&v=2.2.3&downloadable=1
 * <p>
 * The Clarifai website is here:
 * https://www.clarifai.com/developer/
 */
public class PictureRecognizer {

    private String apiKey;
    private int numOfTags = 4;

    public PictureRecognizer(String key) {
        apiKey = key;
    }

    /**
     * Returns the top 3 tag suggestions from the picture located at datapath
     *
     * @param datapath The datapath for the desired picture
     * @return Returns the top 3 tag suggestions from the picture located at datapath
     */
    public ArrayList<String> getSuggestions(String datapath) {

        ArrayList<String> suggestedTags = new ArrayList<>();
        List<Concept> results = callApi(datapath);

        for (int i = 0; i < numOfTags; i++) {
            suggestedTags.add(results.get(i).name());
        }

        return suggestedTags;
    }

    /**
     * Returns a list of Clarifai Concepts for the picture located in datapath.
     * This code was found on this link: https://www.clarifai.com/developer/guide/
     *
     * @param datapath This is the datapath of the picture of interest
     * @return Returns a list of Clarifai Concepts for the picture located in datapath.
     */
    private List<Concept> callApi(String datapath) {
        ClarifaiClient client = new ClarifaiBuilder(apiKey)
                .buildSync();

        List<ClarifaiOutput<Concept>> predictionResults;
        // You can also do client.getModelByID("id") to get your custom models
        predictionResults = client.getDefaultModels().generalModel()
                .predict()
                .withInputs(
                        ClarifaiInput.forImage(new File(datapath)))
                .executeSync()
                .get();

        return predictionResults.get(0).data();
    }
}
