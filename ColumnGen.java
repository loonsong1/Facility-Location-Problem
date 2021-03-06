import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//import ilog.cplex.Cplex.CPXopenCPLEX(Native Method)

import javax.xml.stream.events.EndDocument;

import ilog.concert.*;
import ilog.cplex.*;

public class ColumnGen {
	public static boolean notEndMaster = false;;

	public static void main(String[] args) throws IloException, IOException {

			// creating file
		File file = new File("./files/results.txt");
		PrintWriter w = new PrintWriter(file);

			// Reading files from files & nodes and distance
		 //ReadingFile readingfile = new ReadingFile();
		 //}}
	
			// Initial Configuration
		//InitialSolution itnitialSolution = new InitialSolution();
		//itnitialSolution.InitialCreation();

		// int [][] configuration = Parameters.configuration;
		int n = Parameters.user;
		int m = Parameters.supplier;
		int config = 9;
		int configj = 0;
		int iteration = 0;
		Master problem = null;
		Pricing pp = null;
		
		
		//==========NUMBER OF ITERATION===============
		int iterationcunter = 2000;
		//============================================
		
		//timer starting 
		long startingTime = System.currentTimeMillis();

		do {

			w.println("===============================");
			w.println("Iteration:" + iteration);
			w.println("===============================");
			w.println("===============================");

			System.out.println("===============================");
			System.out.println("Iteration:" + iteration);
			System.out.println("===============================");

			problem = new Master();

			problem.solveMaster(w, notEndMaster);
			w.flush();

		//	for (int o = 0; o < n; o++) {
				config++;

				if (configj < m - 1)
					configj++;
				else
					configj = 0;
				
				
				pp = new Pricing(problem.dualConstraints, configj, config);
				pp.solvePricing(w);
				w.flush();
				pp.addConfig();


				System.out.println("configj="+configj);

				w.println(Arrays.toString(Parameters.configuration[Parameters.configuration.length - 1]));
				w.flush();
//			}

			notEndMaster = !(
					(iteration + 1 < iterationcunter) 
					&& 
					(Pricing.pricingReducedCost < -0.00001));
					
			if (notEndMaster)
				problem.solveMaster(w, notEndMaster);

			iteration++;

		} while (
				iteration < iterationcunter  //);
				&& 
				Pricing.pricingReducedCost < -0.00001);
		
		//Ending time and printing time
		long endingTime = System.currentTimeMillis();
		double time = endingTime - startingTime;
		w.println("time(millisecond)="+time);
		
		
		//Solving integer 
		problem.solveMIP(w);
		w.flush();


	}
}
