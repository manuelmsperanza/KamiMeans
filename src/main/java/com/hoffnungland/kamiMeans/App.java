package com.hoffnungland.kamiMeans;

import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 *
 */
public class App {
	
	private static final Logger logger = LogManager.getLogger(App.class);
	
	public static void main(String[] args) {
		logger.traceEntry();
		String filePath = "./Users-Permissions.csv"; // Replace with the path to your CSV file
		
		
		Matrix kMatrix = new Matrix();
		try {
			kMatrix.loadData(filePath);
			kMatrix.getClusters(50, -1);
			kMatrix.analyzeClusters();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/*List<DoublePoint> points = null;
		try {
			points = loadData(filePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Number of clusters and maximum iterations can be adjusted as needed
		KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(50, -1);
		List<CentroidCluster<DoublePoint>> clusterResults = clusterer.cluster(points);

		// Output the results
		for (int i = 0; i < clusterResults.size(); i++) {
			logger.info("Cluster " + i + ":");
			
			for (DoublePoint point : clusterResults.get(i).getPoints()) {
				logger.info(Arrays.toString(point.getPoint()));
			}
		}*/
		logger.traceExit();
	}

	private static List<DoublePoint> loadData(String filePath) throws IOException {
		logger.traceEntry();
		List<DoublePoint> points = new ArrayList<>();
		CSVFormat csvFormat = CSVFormat.Builder.create().setDelimiter(';').build();
		try (Reader in = new FileReader(filePath)) {
			CSVParser parser = csvFormat.parse(in);
			int countIdx = 0;
			for (CSVRecord record : parser) {
				if(countIdx == 0) {
					countIdx++;
					continue;
				}
				countIdx++;
				double[] permissions = new double[record.size() - 1];
				for (int i = 1; i < record.size(); i++) { // Assuming the first column is user ID
					permissions[i - 1] = Double.parseDouble(record.get(i));
				}
				points.add(new DoublePoint(permissions));
			}
		}
		return logger.traceExit(points);
	}
}
