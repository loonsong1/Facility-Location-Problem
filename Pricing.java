import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.security.auth.login.Configuration;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarBound;
import ilog.cplex.IloCplex;

public class Pricing {
	// parameters
	int n = Parameters.user; // number of users
	int m = Parameters.supplier; // number of facilities

	// int confNum=0; //Configuration number which each iteration we select it

	int configj; // from column generation
	int config; // from column generation
	int[][] configuration = Parameters.configuration;

	double[] D = Parameters.Demand;
	double[][] d = Parameters.distance; // Distance ij
	int[] AW = new int[n];
	int[] AB = new int[n];
	double [] Q = Parameters.Q;
	
	//
	public List<Double> duals = new ArrayList<Double>();

	public static double pricingObjectiveValue=-Double.MAX_VALUE;
	public static double pricingReducedCost=-Double.MAX_VALUE;

	
	IloCplex pricing ;
	
	
	
	public Pricing(List<Double> dualConstraints, int _configj, int _config) {
		// TODO Auto-generated constructor stub
		duals = dualConstraints;
		configj = _configj;
		config = _config;
	}

	// ==============================================
	public void solvePricing(PrintWriter w) {
		
//		for (int i = 0; i < duals.size(); i++) {
//			w.println(duals.get(i));
//			
//		}

		try {

			// w.println("===============================");
			// w.println("pricing");
			// w.println("===============================");
			
			System.out.println("-------------------------------");
			System.out.println("Pricing");
			System.out.println("-------------------------------");
			

			// Define model
			 pricing = new IloCplex();

			// Decision variable
			IloNumVar[] aw = new IloNumVar[n];
			IloNumVar[] ab = new IloNumVar[n];

			// Array for primary
			aw = pricing.boolVarArray(n);

			// Array for backup
			ab = pricing.boolVarArray(n);



			// Objective of pricing
			IloLinearNumExpr ppobjective = pricing.linearNumExpr();
			IloLinearNumExpr reducedCost = pricing.linearNumExpr();


			for (int i = 0; i < n; i++) {
				ppobjective.addTerm(aw[i], d[i][configj]);
				ppobjective.addTerm(ab[i], d[i][configj]);
			}

			// Dual1
			//ppobjective.add(-duals.get(configj));
			//reducedCost.add(-duals.get(configj));
			
			
			// Dual4
			for (int i = 0; i < n; i++) {
				int k = m + i ;
				ppobjective.addTerm(aw[i], -duals.get(k));
				reducedCost.addTerm(aw[i], -duals.get(k));
			}
			
			// Dual5-1
			for (int i = 0; i < n; i++) {
				int k = m+n + (i* m) + configj;
				ppobjective.addTerm(ab[i], -duals.get(k));
				ppobjective.addTerm(aw[i], -duals.get(k));
				reducedCost.addTerm(ab[i], -duals.get(k));
				reducedCost.addTerm(aw[i], -duals.get(k));
			}
			
			
			// Dual5-2
			for (int j = 0; j < m; j++) {
				if (j != configj) {
					for (int i = 0; i < n; i++) {
						int k = m+n+(i*m)+configj;
						ppobjective.addTerm(ab[j], -duals.get(k));
						reducedCost.addTerm(ab[j], -duals.get(k));

					}
				}
			}

			
			//Dual6-1
			for (int i = 0; i < n; i++) {
				int k = m+n+(n*m)+(i*m)+configj;
				ppobjective.addTerm(aw[i], -duals.get(k));
				ppobjective.addTerm(ab[i], +duals.get(k));
				reducedCost.addTerm(aw[i], -duals.get(k));
				reducedCost.addTerm(ab[i], +duals.get(k));
			}
			

			//Dual6-1
			for (int j = 0; j < m; j++) {
				if (j != configj) {
					for (int i = 0; i < n; i++) {
						int k = m+n+(n*m)+(i*m)+configj;
						ppobjective.addTerm(ab[j], -duals.get(k));
						reducedCost.addTerm(ab[j], -duals.get(k));
					}
				}
			}

				

//			IloNumExpr exp = pricing.sum(ppobjective, 3);
//			pricing.addMinimize(exp);
			pricing.addMinimize(ppobjective);

			// Constraints

			// constraint #1
			IloLinearNumExpr sumCons1 = pricing.linearNumExpr();

			for (int i = 0; i < n; i++) {
				sumCons1.addTerm(aw[i], D[i]);
				sumCons1.addTerm(ab[i], D[i]);	
			}
			pricing.addLe(sumCons1, Q[configj]);
			// IloLinearNumExpr sumwb = pricing.linearNumExpr();

			// constraint #2
			for (int i = 0; i < n; i++) {
				IloLinearNumExpr sumwb = pricing.linearNumExpr();
				sumwb.addTerm(aw[i], 1);
				sumwb.addTerm(ab[i], 1);
				pricing.addLe(sumwb, 1);
			}

		
			
			// Solve the model
			if (pricing.solve()) {
				w.println("===============================");
				w.println("ppobjective=" + pricing.getObjValue());
				pricingObjectiveValue=pricing.getObjValue();
				
				
				for (int l = 0; l < n; l++) {
					// System.out.println("aw[" + l + "]=" +
					// pricing.getValue(aw[l]));
					AW[l] = (int) pricing.getValue(aw[l]);

				}
				for (int l = 0; l < n; l++) {
					// System.out.println("ab[" + l + "]=" +
					// pricing.getValue(ab[l]));
					AB[l] = (int) pricing.getValue(ab[l]);
				}
//*************************************************************************************
				w.println("Reduced Cost = " +pricing.getValue(reducedCost));
				pricingReducedCost=pricing.getValue(reducedCost);



			}

			// Close problem
			pricing.end();
		}

		catch (IloException exc) {
			exc.printStackTrace(); // printing error
		}

	}
	// ==============================================

	// Adding new configuration to master problem
	public void addConfig() {
		// TODO Auto-generated method stub
		Parameters.configuration = Arrays.copyOf(Parameters.configuration, Parameters.configuration.length + 1);
		Parameters.configuration[Parameters.configuration.length - 1] = new int[2 * m + 2];
		for (int i = 0; i < 2 * m + 2; i++) {
			if (i == 0)
				Parameters.configuration[Parameters.configuration.length - 1][i] = config;
			else if (i == 1)
				Parameters.configuration[Parameters.configuration.length - 1][i] = configj;
			else if (i >= 2 && i <= m + 2)
				for (int j = 0; j < m; j++) {
					if (j == i - 2)
						Parameters.configuration[Parameters.configuration.length - 1][i] = AW[j];
					// System.out.println(Parameters.configuration[Parameters.configuration.length
					// - 1][i]);
				}
			else
				for (int j = 0; j < m; j++)
					if (j == i - (m + 2))
						Parameters.configuration[Parameters.configuration.length - 1][i] = AB[j];
		}
	}
}
