import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.cf.taste.recommender.IDRescorer;


/**
 * 
 */

/**
 * @author Ninhsth
 *
 */
public class SampleRecommender {
	private static BufferedReader br;

	static void genData(){
		PrintWriter writer;
		try {
			writer = new PrintWriter("input/data-2.csv", "UTF-8");
			br = new BufferedReader(new FileReader("input/u.data.txt")); 
			int j=1;    
			for(String line; (j<1000000)&& (line = br.readLine()) != null; ) {
			        // process the line.
					String[] fields = line.split("\t");
					int[] numFields = new int[fields.length];
					for (int k=0; k< fields.length;k++){
						numFields[k]=Integer.parseInt(fields[k]);
					}
					writer.println(numFields[0] + "," + numFields[1] + "," + numFields[2]);
			    	//System.out.println("["+line + "] Newline [" + line.trim().substring(0, line.length()) +"]");	
			    	j++;
			    }
			    // line is not visible here.
			writer.close();
			System.out.println("Done");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static void appendData(){
		PrintWriter out;
		try {
			 out = new PrintWriter(new BufferedWriter(new FileWriter("input/data-2.csv", true)));
			 // Add data to increase score of item
			 int itemid = 114;
			 System.out.println("Add data to increase score of item " + itemid + "...");
			 out.println("658," + itemid + ",5");
			 out.println("658," + itemid + ",5");
			 out.println("2," + itemid + ",5");
			 out.println("665," + itemid + ",5");
			 out.println("5," + itemid + ",5");
			 
			 
			out.close();
			
			System.out.println("You will see score of item " + itemid + " increased if it is under max (5) and new records not exist");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static void testRefresh(){
		try {
			boolean transpose = false;
			long minReloadIntervalMS = 6000;
			int userid = 196;
			long startTime; 
		    long stopTime;
		  	    
		    startTime = System.currentTimeMillis();
			DataModel model = new FileDataModel(new File("input/data-2.csv"), transpose, minReloadIntervalMS);
			//DataModel model = new FileDataModel(new File("/Users/Ninhsth/Downloads/ml-100k/u.data.5.csv"), transpose, minReloadIntervalMS);
			stopTime = System.currentTimeMillis(); System.out.println(stopTime - startTime + " (ms) to load data model");
			
		    startTime = System.currentTimeMillis();
			UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
			stopTime = System.currentTimeMillis(); System.out.println(stopTime - startTime + " (ms) to process similarity");

			//System.out.println("similarity 2 and 1:" + similarity.userSimilarity(2, 1));
			//System.out.println("similarity 2 and 2:" + similarity.userSimilarity(2, 2));
			//System.out.println("similarity 2 and 3:" + similarity.userSimilarity(2, 3));
			//System.out.println("similarity 2 and 4:" + similarity.userSimilarity(2, 4));
					
			System.out.println("neighborhood of user "+ userid + ":");
		    startTime = System.currentTimeMillis();
			UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
			stopTime = System.currentTimeMillis(); System.out.println(stopTime - startTime + " (ms) to process neighborhood");
			
			System.out.println("neighborhood of user "+ userid + ":");
			long neighborhoodOf2[] = neighborhood.getUserNeighborhood(userid);
			stopTime = System.currentTimeMillis(); System.out.println(stopTime - startTime + " (ms) to get neighborhood of a user");

			for (long i:neighborhoodOf2 )
				{
					//System.out.println(i);								
				}
			
			// recommend on all
			System.out.println(new Timestamp(new java.util.Date().getTime())+ " - Recommended for user " + userid + " on items: top items:");
		    startTime = System.currentTimeMillis();
			UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
			stopTime = System.currentTimeMillis(); System.out.println(stopTime - startTime + " (ms) to init recommender");
			
		    startTime = System.currentTimeMillis();
			List<RecommendedItem> recommendations = recommender.recommend(userid, 10);
			stopTime = System.currentTimeMillis(); System.out.println(stopTime - startTime + " (ms) to recommend for a user");

			for (RecommendedItem recommendation : recommendations) {
			  System.out.println(recommendation);
			}	
			
			///// stop here, update input file then continue to see new data
			// recommend on new items
			System.out.println(new Timestamp(new java.util.Date().getTime())+ " - Recommended for user " + userid + " on NEW items: top items:");
			appendData();
			
			// It treats new model completely different with the old, even though we put old neighborhood and similarity.
			// DataModel model_new = new FileDataModel(new File("input/data-new-1.csv"));
			// But we can refresh recommender after update input data
			// UserBasedRecommender recommender_new = new GenericUserBasedRecommender(model, neighborhood, similarity);
			//IDRescorer idrescorer = new IDRescorer();
//			DataModel cur_model = new FileDataModel(null);
//			cur_model = model;
			//System.out.println(new Timestamp(new java.util.Date().getTime())+ " - before refresh datamodel");
			
			Collection<Refreshable> alreadRefreshModel = new ArrayList<Refreshable>();
			alreadRefreshModel.add(model);			
		    startTime = System.currentTimeMillis();
			model.refresh(alreadRefreshModel); // will read and process all lines in file
			stopTime = System.currentTimeMillis(); System.out.println(stopTime - startTime + " (ms) to get refresh model");

			//System.out.println(new Timestamp(new java.util.Date().getTime())+ " - after refresh datamodel");

			//System.out.println(new Timestamp(new java.util.Date().getTime())+ " - Before refresh recommender");
		    startTime = System.currentTimeMillis();
			Collection<Refreshable> alreadRefreshRecommender =  new ArrayList<Refreshable>();
			alreadRefreshRecommender.add(recommender);	
			recommender.refresh(alreadRefreshRecommender); // will process on all data (users, items and preferences)
			stopTime = System.currentTimeMillis(); System.out.println(stopTime - startTime + " (ms) to get refresh recommender NEW");
			//System.out.println(new Timestamp(new java.util.Date().getTime())+ " - After refresh recommender");

		    startTime = System.currentTimeMillis();
			List<RecommendedItem> recommendations_new = recommender.recommend(userid, 10);
			stopTime = System.currentTimeMillis(); System.out.println(stopTime - startTime + " (ms) to get recommender a user NEW");

			for (RecommendedItem recommendation : recommendations_new) {
			  System.out.println(recommendation);
			}	
			System.out.println(new Timestamp(new java.util.Date().getTime())+ " - Done");

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TasteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// genData();
		testRefresh();
		//appendData();
		
		
	}
}
