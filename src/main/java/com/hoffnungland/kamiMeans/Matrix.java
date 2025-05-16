package com.hoffnungland.kamiMeans;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Matrix {

	public List<String> indexList = new ArrayList<String>();
	public List<String> headerList = new ArrayList<String>();
	public List<DoublePoint> points = new ArrayList<DoublePoint>();
	
	private List<CentroidCluster<DoublePoint>> clusterResults;
	
	private static final Logger logger = LogManager.getLogger(Matrix.class);
	
	public void loadData(String filePath) throws IOException {
		logger.traceEntry();
		
		CSVFormat csvFormat = CSVFormat.Builder.create().setDelimiter(';').build();
		try (Reader in = new FileReader(filePath)) {
			CSVParser parser = csvFormat.parse(in);
			int countIdx = 0;
			for (CSVRecord record : parser) {
				if(countIdx == 0) {
					for (int i = 1; i < record.size(); i++) {
						this.headerList.add(record.get(i));
					}
				} else {
					this.indexList.add(record.get(0));
					double[] permissions = new double[record.size() - 1];
					for (int i = 1; i < record.size(); i++) { // Assuming the first column is user ID
						permissions[i - 1] = Double.parseDouble(record.get(i));
					}
					this.points.add(new DoublePoint(permissions));
				}
				countIdx++;
			}
		}
		
		logger.traceExit();
	}
	
	public void getClusters(int k, int maxIterations) throws IOException {
		logger.traceEntry();
		KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(k, maxIterations);
		this.clusterResults = clusterer.cluster(this.points);
		
		for (int clusterIdx = 0; clusterIdx < this.clusterResults.size(); clusterIdx++) {
			
			CSVPrinter csvPrinterRawMatrix = new CSVPrinter(new FileWriter("raw_cluster_" + clusterIdx + ".csv"), CSVFormat.DEFAULT);
			csvPrinterRawMatrix.printRecord(this.headerList.toArray());
			
			for (DoublePoint point : this.clusterResults.get(clusterIdx).getPoints()) {
				double[] dPoints = point.getPoint();
				 Object[] doubleObjects = new Object[dPoints.length];
		            for (int dPointsIdx = 0; dPointsIdx < dPoints.length; dPointsIdx++) {
		                doubleObjects[dPointsIdx] = dPoints[dPointsIdx];
		            }
				
				csvPrinterRawMatrix.printRecord(doubleObjects);
			}
			
			csvPrinterRawMatrix.close();
		}
		
		
		logger.traceExit();
	}
	
	public void analyzeClusters() throws IOException {
		logger.traceEntry();
		
		CSVPrinter csvPrinterHeaderCluster = new CSVPrinter(new FileWriter("header_cluster.csv"), CSVFormat.DEFAULT);
		String[] hClusterHead = { "cluster", "header" };
		csvPrinterHeaderCluster.printRecord(hClusterHead);
		CSVPrinter csvPrinterIndexCluster = new CSVPrinter(new FileWriter("index_cluster.csv"), CSVFormat.DEFAULT);
		String[] iClusterHead = { "cluster", "index" };
		csvPrinterIndexCluster.printRecord(iClusterHead);
		
		String[] nonClusteredHeaderList = this.convertListToStringArray(this.headerList);
		String[] nonClusteredIndexList = this.convertListToStringArray(this.indexList);
		
		for (int clusterIdx = 0; clusterIdx < this.clusterResults.size(); clusterIdx++) {
			
			String[] headerListCopy = this.convertListToStringArray(this.headerList);
			String[] indexListCopy = this.convertListToStringArray(this.indexList);
				
			for (DoublePoint point : this.clusterResults.get(clusterIdx).getPoints()) {
				
				double[] dPoints = point.getPoint();
				
				for (int dPointsIdx = 0; dPointsIdx < dPoints.length; dPointsIdx++) {
					if(dPoints[dPointsIdx] == 0.0) {
						headerListCopy[dPointsIdx] = null; //Reset the header when the value does not belong to the cluster
						/*} else {
						nonClusteredHeaderList[dPointsIdx] = null;*/ //Reset the header for non clustered values
						
					}
				}
				
			}
			logger.info("Cluster " + clusterIdx + ":");
			logger.info("Header");
			boolean skipHeader = true; 
			for (int headerIdx = 0; headerIdx < headerListCopy.length; headerIdx++) {
				if(headerListCopy[headerIdx] != null) {
					skipHeader = false;
					logger.info(headerListCopy[headerIdx]);
					nonClusteredHeaderList[headerIdx] = null; //Reset the header for non clustered values
					String[] headerClusterRecord = {Integer.toString(clusterIdx), headerListCopy[headerIdx]};
					csvPrinterHeaderCluster.printRecord(headerClusterRecord);
					
					for(int pointIdx = 0; pointIdx < indexListCopy.length; pointIdx++) {
						if(indexListCopy[pointIdx] != null) {
							double pointValue = this.points.get(pointIdx).getPoint()[headerIdx];
							if(pointValue == 0) {
								indexListCopy[pointIdx] = null;
							/*} else {
								nonClusteredIndexList[pointIdx] = null;*/
							}
						}
					}
				}
			}
			if(skipHeader) {
				logger.warn("Skip cluster " + clusterIdx + ":");
			} else {
				logger.info("Index");
				for(int pointIdx = 0; pointIdx < indexListCopy.length; pointIdx++) {
					if(indexListCopy[pointIdx] != null) {
						logger.info(indexListCopy[pointIdx]);
						nonClusteredIndexList[pointIdx] = null;
						
						String[] indexClusterRecord = {Integer.toString(clusterIdx), indexListCopy[pointIdx]};
						csvPrinterIndexCluster.printRecord(indexClusterRecord);
					}
				}
			}
		}
		
		logger.info("Non clustered header:");
		for (int headerIdx = 0; headerIdx < nonClusteredHeaderList.length; headerIdx++) {
			if(nonClusteredHeaderList[headerIdx] != null) {
				logger.info(nonClusteredHeaderList[headerIdx]);
				String[] headerClusterRecord = {"-1", nonClusteredHeaderList[headerIdx]};
				csvPrinterHeaderCluster.printRecord(headerClusterRecord);
			}
		}
		
		logger.info("Non clustered index:");
		for (int pointIdx = 0; pointIdx < nonClusteredIndexList.length; pointIdx++) {
			if(nonClusteredIndexList[pointIdx] != null) {
				logger.info(nonClusteredIndexList[pointIdx]);
				String[] indexClusterRecord = {"-1", nonClusteredIndexList[pointIdx]};
				csvPrinterIndexCluster.printRecord(indexClusterRecord);
			}
		}
		
		csvPrinterHeaderCluster.close();
		csvPrinterIndexCluster.close();
		logger.traceExit();
	}
	
	private String[] convertListToStringArray(List<String> list) {
		logger.traceEntry();
        // Create an array of the same size as the list
        String[] array = new String[list.size()];

        // Use the toArray method to fill the array
        return logger.traceExit(list.toArray(array));
    }
	
}
