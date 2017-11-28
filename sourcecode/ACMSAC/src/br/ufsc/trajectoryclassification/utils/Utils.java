package br.ufsc.trajectoryclassification.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.ufsc.trajectoryclassification.model.bo.dmbt.IDistanceMeasureBetweenTrajectories;
import br.ufsc.trajectoryclassification.model.dao.TrajectoryDAO;
import br.ufsc.trajectoryclassification.model.vo.IPoint;
import br.ufsc.trajectoryclassification.model.vo.ISubtrajectory;
import br.ufsc.trajectoryclassification.model.vo.ITrajectory;
import br.ufsc.trajectoryclassification.model.vo.Trajectory;
import br.ufsc.trajectoryclassification.model.vo.description.Description;
import br.ufsc.trajectoryclassification.model.vo.features.IFeature;
import br.ufsc.trajectoryclassification.model.vo.features.Numeric;

public class Utils {

	public static final String CURRENT_DIR = "D:/Users/andres/git_projects/datasets/taxis3000/";

	public static double getMinDistance(List<Double> distances) {

		double min = Double.MAX_VALUE;

		double distance = 0;

		for (int i = 0; i < distances.size(); i++) {
			distance = distances.get(i);
			if (distance < min)
				min = distance;
		}

		return min;
	}

	public static double mean(List<Double> values) {

		double sum = 0;

		for (int i = 0; i < values.size(); i++) {
			sum += values.get(i);
		}

		return sum / values.size();
	}

	public static double rad2deg(double rad) {
		return rad * 180.0 / Math.PI;
	}

	public static double km2deg(double km) {
		double R = 6371.1;
		return rad2deg(km / R);
	}

	public static double M100_IN_DEGREES = km2deg(0.1);	

	public static double[][] append(double[][] a, double[][] b) {
		if (a == null){
			double[][] result = new double[b.length][];
			System.arraycopy(b, 0, result, 0, b.length);
			return result;
		}
		else if (b == null){
			double[][] result = new double[a.length][];
			System.arraycopy(a, 0, result, 0, a.length);
			return result;
		}
		else {
			double[][] result = new double[a.length + b.length][];
			System.arraycopy(a, 0, result, 0, a.length);
			System.arraycopy(b, 0, result, a.length, b.length);
			return result;
		}
	}

	public static double normalize(double value, double max) {
		// Este m�todo est� preparado para que, se o valor max for negativo
		// Ele n�o realiza a normaliza��o. Assim, estes ser�o os valores por
		// padr�o
		// Para as vari�veis de max.
		if (max < 0)
			return value;
		else if (value > max)
			return 1;
		else
			return value / max;

	}

	public static void statsMemory() {

		int mb = 1024 * 1024;

		// get Runtime instance
		Runtime instance = Runtime.getRuntime();

		System.out.println("***** Heap utilization statistics [MB] *****\n");

		// available memory
		System.out.println("Total Memory: " + instance.totalMemory() / mb);

		// free memory
		System.out.println("Free Memory: " + instance.freeMemory() / mb);

		// used memory
		System.out.println("Used Memory: " + (instance.totalMemory() - instance.freeMemory()) / mb);

		// Maximum available memory
		System.out.println("Max Memory: " + instance.maxMemory() / mb);

	}

	public static double[][] getDistanceMatrix(List<ITrajectory> trajectories,
			IDistanceMeasureBetweenTrajectories dmbt) {

		int n = trajectories.size();

		double[][] data = new double[n][n];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				data[i][j] = dmbt.getDistance(trajectories.get(i), trajectories.get(j));
			}
		}
		
		return data;

	}

	public static double[][] getDistanceMatrix(List<ITrajectory> train, List<ITrajectory> test,
			IDistanceMeasureBetweenTrajectories dmbt) {

		int n = test.size();
		int m = train.size();

		double[][] data = new double[n][m];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				data[i][j] = dmbt.getDistance(test.get(i), train.get(j));
			}			
		}

		return data;

	}

	public static double estimateMaxValueFromDistanceMatrix(double[][] data) {

		double sum = 0;
		int n = Math.round(data.length / 10);
		int m = Math.round(data[0].length / 10);

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				sum += data[i][j];
			}
		}

		System.out.println(sum);
		return sum / (n * m);
	}

	public static void distanceMatrixToCSV(String filename, double[][] distanceMatrix,	List<String> distanceMatrixLabels) {

		try {
			FileWriter file = new FileWriter(filename);
			BufferedWriter writer = new BufferedWriter(file);
			String header = new String();
			// Doing the header
			for (int i = 0; i < distanceMatrix[0].length; i++) {
				header += "att" + (i+1) + ",";
			}
			header += "class";
			writer.write(header + System.getProperty("line.separator"));
			
			// Doing the data
			for (int i = 0; i < distanceMatrix.length; i++) {
				double[] ds = distanceMatrix[i];
				writer.write(Arrays.toString(ds).replaceAll("[\\[|\\]|\\s]", "") + "," + "\"" + distanceMatrixLabels.get(i) + "\""
						+ System.getProperty("line.separator"));							
			}
			
			writer.flush();						
			writer.close();			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void stopIfErrorValues(Map<String,IFeature> features){
		for (String key : features.keySet()) {
			Double value = ((Numeric) features.get(key)).getValue();
			if (value.isNaN() || value.isInfinite()){
				System.out.println(key+" "+value);
				System.exit(1);
			}
		}
	}

	public static List<ITrajectory> loadTrajectories(String dirPath, Description description){
		
		String filepath = dirPath + ".zip"; 
		
		if (!new File(filepath).exists()) return new ArrayList<>();
		
		UnzipUtility.unzip(filepath, dirPath);
		
		List<ITrajectory> trajectories = new TrajectoryDAO().loadFromDir(dirPath,description);
		
		try {
			FileUtils.deleteDirectory(new File(dirPath));			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return trajectories;	
		
	}

	public static void writeShapelets(List<ISubtrajectory> shapelets, String filepath) {

		BufferedWriter writer;

		try {
			
			File file = new File(filepath);
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file));

			for (ISubtrajectory subtrajectory : shapelets) {
				writer.write(subtrajectory.toString() + System.getProperty("line.separator"));
			}

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void putAttributeIntoTrajectories(List<ITrajectory> trajectories, ISubtrajectory shapelet) {

		String attributeName = "sh_TID" + shapelet.getTrajectory().getTid() + 
									"_START" + shapelet.getStart() + 
									"_SIZE" + shapelet.getSize() + 
									"_CLASS" + shapelet.getTrajectory().getLabel();

		Double maxDistance = (Double) shapelet.getQuality().getData().get("maxDistance");
		Double splitpoint = (Double) shapelet.getQuality().getData().get("splitpoint");
		
		for (int i = 0; i < trajectories.size(); i++) {
			double distance = shapelet.getDistances()[i];
			distance = (distance == Double.MAX_VALUE) ? maxDistance: distance;
			distance = (distance <= splitpoint) ? distance / splitpoint : 1 + distance / maxDistance;
			trajectories.get(i).getAttributes().put(attributeName, distance);
		}
	}
	
	public static void writeTrajectoriesToGSON(List<ITrajectory> trajectories, Description description, String filePath){

		try {			
			File file = new File(filePath);
			file.getParentFile().mkdirs();
			
			FileWriter fileWriter = new FileWriter(filePath);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(trajectories, fileWriter);
			fileWriter.write(System.getProperty("file.separator"));
			fileWriter.close();

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	public static void writeShapeletsToGSON(List<ITrajectory> trajectories, List<ISubtrajectory> shapelets, Description description, String filePath){
		
		List<Map<String,Object>> classOfTrajectories = new ArrayList<>();
		
		for (ITrajectory t : trajectories) {			
			Map<String, Object> classOfT = new HashMap<>();
			classOfT.put("tid", t.getTid());
			classOfT.put("label", t.getLabel());			
			classOfTrajectories.add(classOfT);
		}
				
		List <SubtrajectoryToGSON> subtrajectoryToGSONs = new ArrayList<>();
		
		for (ISubtrajectory shapelet : shapelets) {
			subtrajectoryToGSONs.add(SubtrajectoryToGSON.fromSubtrajectory(shapelet,description));		
			//System.out.println(SubtrajectoryToGSON.fromSubtrajectory(shapelet).getFeatures());
		}
		
		Map<String, Object> toGSON = new HashMap<>();
		toGSON.put("classes", classOfTrajectories);
		toGSON.put("shapelets", subtrajectoryToGSONs);
		
		try {
			File file = new File(filePath);
			file.getParentFile().mkdirs();
			
			FileWriter fileWriter = new FileWriter(filePath);
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
			gson.toJson(toGSON, fileWriter);
			fileWriter.close();

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.getLocalizedMessage();
			e.printStackTrace();
		} 
		
				
	}
	
	
	public static void writeAttributesCSV(List<ITrajectory> trajectories, String filepath) {
		// TODO Auto-generated method stub
		BufferedWriter writer;

		try {
			
			File file = new File(filepath);
			file.getParentFile().mkdirs();
			writer = new BufferedWriter(new FileWriter(file));
						
			String header = (!trajectories.get(0).getFeatures().keySet().isEmpty()) ? 
					trajectories.get(0).getFeatures().keySet().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
			header += (!trajectories.get(0).getAttributes().keySet().isEmpty()) ?
					trajectories.get(0).getAttributes().keySet().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : ""; 
			
			header += "class" + System.getProperty("line.separator");
			
			writer.write(header);
			
			for (ITrajectory trajectory : trajectories) {
				
				String line = (!trajectory.getFeatures().values().isEmpty()) ?
								trajectory.getFeatures().values().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
				line += (!trajectory.getAttributes().values().isEmpty()) ?
						trajectory.getAttributes().values().toString().replaceAll("[\\[|\\]|\\s]", "") + "," : "";
				
				line += "\"" + trajectory.getLabel() + "\""+ System.getProperty("line.separator");
				
				writer.write(line);
			
			}

			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void dimensionalityReduction(List<ITrajectory> trajectories, double rate) {
		
		if (rate <= 0 || rate >= 1) return;
		
		// TODO Auto-generated method stub
		Random random = new Random(1);
		for (int i = 0; i < trajectories.size(); i++) {
			ITrajectory t = trajectories.get(i);
			int size = t.getData().size();
			int newSize = size - (int) Math.round(rate * size);
			
			List<Integer> list = IntStream.range(0, size).boxed().collect(Collectors.toList());
			Collections.shuffle(list,random);
			list = new ArrayList<>(list.subList(0, newSize));
			Collections.sort(list);
						
			List<IPoint> newData = new ArrayList<>();			
			for (Integer position : list) {
				newData.add(t.getData().get(position));
			}
			
			trajectories.set(i, new Trajectory(t.getTid(),newData, t.getLabel()));
		}
		
	}

	public static void selectingPoints(List<ITrajectory> trajectories, int maxPoints) {
		
		for (int i = 0; i < trajectories.size(); i++) {
			ITrajectory t = trajectories.get(i);				
			int size = t.getData().size();
			
			if (size > maxPoints){
				int fromIndex = maxPoints;
				int toIndex = size;
				t.getData().removeAll(  t.getData().subList(fromIndex, toIndex) );				
			}			
			
		}
		
	}
	
	public static List<List<Integer>> createIndexFoldsCV(int datasetSize, int folds){
		
		if (datasetSize < folds){
			System.err.println("Very little dataset to perform cross validations");
			System.exit(1);
		}
		
		List<Integer> integers =  IntStream.range(0, datasetSize).boxed().collect(Collectors.toList()); 
		Collections.shuffle(integers);		
		
		int partitionSize = integers.size() / folds;
		int remaind = integers.size() % folds;		
		
		List<List<Integer>> partitions = new ArrayList<>();
		
		int fromIndex = 0;
		int toIndex = partitionSize + ((remaind > 0) ? 1 : 0);
		for (int i = 0; i < folds; i++) {
			 partitions.add(new ArrayList<>(integers.subList(fromIndex, toIndex)));
			 fromIndex = toIndex;
			 toIndex += partitionSize + ((remaind > (i+1)) ? 1 : 0);
		}
		
		return partitions;
	}


}
