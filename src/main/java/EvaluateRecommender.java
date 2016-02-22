import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;

/**
 * 
 */

/**
 * @author Ninhsth
 *
 */
public class EvaluateRecommender {
	private static class MyRecommenderBuilder implements  RecommenderBuilder{
		
		public Recommender buildRecommender(DataModel dataModel) throws TasteException {
			// TODO Auto-generated method stub
			UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
			UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel);
			return new GenericUserBasedRecommender(dataModel, neighborhood, similarity);	
		}		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataModel model;
		try {
			String filePath ="/Users/Ninhsth/Downloads/ml-100k/u.data.1000.csv";
			model = new FileDataModel(new File(filePath));
		
			RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
			RecommenderBuilder builder = new MyRecommenderBuilder();
			
			double result;
			result = evaluator.evaluate(builder, null, model, 0.9, 0.1);
			System.out.println(result);
			// NaN means there was no data at all on which to base an estimate. That's generally a symptom of data sparseness. It should be rare, and happen only for users with data that's very small or disconnected from others'.
			//I personally think it's not such a big deal unless it's a really significant percentage (20%+?) I'd worry more if you couldn't generate any recs at all for many users
			
			
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
					

	}

}
