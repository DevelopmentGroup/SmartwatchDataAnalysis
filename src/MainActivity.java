import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;


public class MainActivity {
	
	static boolean handwashing;
	
	static String outputName = "features.csv";
	


	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		PrintWriter writer = new PrintWriter(outputName, "UTF-8");
		writer.println("mean_x, std_x, mean_y, std_y, mean_z, std_z, Activity");
		
		Files.walk(Paths.get("C:/Users/Joshua/workspace/SmartwatchDataAnalysis/src/data")).forEach(filePath -> {
		    if (Files.isRegularFile(filePath)) {
		        //System.out.println(filePath);
		    	
		        
		    	String fileName = filePath.getFileName().toString();
		    	
		    	//System.out.println(fileName);
		        
		    	if (fileName.contains("activity2")){
		    		handwashing = false;
		    	} else {
		    		handwashing = true;
		    	}
		    	
		    	try {
					run(fileName, writer);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	
		    	
		    	
		    	
		        
		    }
		});
		
		/*
		Scanner reader = new Scanner(System.in);
		System.out.println("Handwashing? 1 for yes, 0 for no: ");
		int n = reader.nextInt();
		if(n == 1){
			System.out.println("1: Handwashing");
			handwashing = true;
		} else if (n == 0){
			System.out.println("0: Not handwashing");
			handwashing = false;
		}
		*/
		/*
		System.out.println("Input File Name: ");
		
		inputName = reader.next();
		
		System.out.println("Output file name: ");
		
		outputName = reader.next();
		*/
		
		//run();
		
		writer.close();

	}
	
	
	
	
	public static void run(String inputName, PrintWriter writer) throws FileNotFoundException, UnsupportedEncodingException{
		String csvFile = "C:/Users/Joshua/workspace/SmartwatchDataAnalysis/src/data/" + inputName;
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
		
		
		ArrayList<String[]> allData = new ArrayList<String[]>();
		
		try{
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null){
				String[] properties = line.split(csvSplitBy);
				String timestamp = properties[0];
					Integer ts = Integer.parseInt(timestamp);
					ts = ts/1000;
					timestamp = String.valueOf(ts);
				
				String xvalues = properties[1];
				String yvalues = properties[2];
				String zvalues = properties[3];
				
				//System.out.println(timestamp + ": " + "X(" + xvalues + "), Y(" + yvalues + "), Z(" + zvalues + ")");
				String[] tempDataRow = {timestamp, xvalues, yvalues, zvalues};
				allData.add(tempDataRow);
			}
		} catch (FileNotFoundException e){
			System.out.println("FILE NOT FOUND EXCEPTION");
			e.printStackTrace();
		} catch (IOException e){
			System.out.println("IO EXCEPTION");
			e.printStackTrace();
		} finally {
			if (br != null){
				try {
					br.close();
				} catch (IOException e){
					e.printStackTrace();
				}
			}
		}
		
		
		
		//loop through all of the data
		float runningSumX = 0;
		float runningSumY = 0;
		float runningSumZ = 0;
		ArrayList<Double> xdata = new ArrayList<Double>();
		ArrayList<Double> ydata = new ArrayList<Double>();
		ArrayList<Double> zdata = new ArrayList<Double>();
		ArrayList<double[]> windowData = new ArrayList<double[]>();
		
		float runningCount = 0;
		for (int i = 1; i < allData.size(); i++){
			if(allData.get(i)[0].equals(allData.get(i-1)[0])){
				runningSumX += Float.parseFloat(allData.get(i)[1]);
				//xdata.add(Integer.parseInt(allData.get(i)[1]));
				runningSumY += Float.parseFloat(allData.get(i)[2]);
				runningSumZ += Float.parseFloat(allData.get(i)[3]);
				
				xdata.add(Double.parseDouble(allData.get(i)[1]));
				ydata.add(Double.parseDouble(allData.get(i)[2]));
				zdata.add(Double.parseDouble(allData.get(i)[3]));
				
				double[] tempData = {Double.parseDouble(allData.get(i)[1]), Double.parseDouble(allData.get(i)[2]), Double.parseDouble(allData.get(i)[3])};
				windowData.add(tempData);
				
				runningCount += 1;
			} else {
				float averageX = runningSumX / runningCount;
				float averageY = runningSumY / runningCount;
				float averageZ = runningSumZ / runningCount;
				
				
				System.out.println("Numbers: ");
				/*for (int j = 0; j < xdata.size(); j++){
					System.out.print(xdata.get(j) + ", ");
				}*/
				
				System.out.println("Averages: ");
				System.out.println(allData.get(i)[0] + ": " + String.valueOf(averageX) + ", " + String.valueOf(averageY) + ", " + String.valueOf(averageZ));
				
				double stdvX = getStdDev(averageX, windowData, 0);
				double stdvY = getStdDev(averageY, windowData, 1);
				double stdvZ = getStdDev(averageZ, windowData, 2);
				
				System.out.println("Standard Deviations: ");
				System.out.println(allData.get(i-1)[0] + ": " + String.valueOf(stdvX) + ", " + String.valueOf(stdvY) + ", " + String.valueOf(stdvZ));
				
				
				
				//mean_x, std_x, mean_y, std_y, mean_z, std_z, Activity
				String activity;
				if(handwashing){
					activity = "hand_wash";
				} else {
					activity = "not_hand_wash";
				}
				writer.println(averageX + "," + stdvX + "," + averageY + "," + stdvY + "," + averageZ + "," + stdvZ + "," + activity);
				
				
				//reset running numbers
				runningSumX = 0;
				runningSumY = 0;
				runningSumZ = 0;
				
				
				xdata.clear();
				ydata.clear();
				zdata.clear();
				windowData.clear();
				
				
				
				
				
				
				
				
				runningCount = 1;
			}
		}
		
		
		
	}
	
	
	
	
	public static float getVariance(float mean, ArrayList<double[]> allData, int x){
		//float mean = getMean();
		float vari = 0;
		
		for(int i = 0; i < allData.size(); i++){
			vari += (mean - allData.get(i)[x]) * (mean - allData.get(i)[x]);
			
		}
		
		return vari/allData.size();
	}
	
	public static double getStdDev(float mean, ArrayList<double[]> allData, int x){
		return Math.sqrt(getVariance(mean, allData, x));
	}
}

