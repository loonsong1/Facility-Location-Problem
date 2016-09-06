import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import ilog.concert.IloConversion;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloRange;
import ilog.concert.*;
import ilog.cplex.*;
//import ilog.cplex.IloCplex.UnknownObjectException;
//import ilog.cplex.IloCplex.CPXopenCPLEX;
import ilog.cplex.IloCplex;

public class Master {

	// parameters
	int n = Parameters.user; // number of users
	int m = Parameters.supplier; // number of facilities
	int p = Parameters.p; // number of facility to open
	double[] D = Parameters.Demand; // Demand
	double[][] d = Parameters.distance; // Distance ij
	double[] Q = Parameters.Q; // Capacity of facility
	double[] r = Parameters.r;
	double[] s = Parameters.s;
	double B = Parameters.Budget; // budget
	double[] pr = Parameters.pr; // probability of facility failure
	int[][] configuration = Parameters.configuration;
	//Duality
	List<IloRange> constraints = new ArrayList<IloRange>();
	List<Double> dualConstraints = new ArrayList<Double>();
	
	List<IloConversion> mipConversion = new ArrayList<IloConversion>();

	// Decision variable
	IloNumVar[] z = new IloNumVar[configuration.length];
	IloNumVar[] y = new IloNumVar[m];
	IloNumVar[] x = new IloNumVar[m];
	IloNumVar[] Qw = new IloNumVar[m];
	IloNumVar[] Qb = new IloNumVar[m];

	IloCplex master;

	// ==============================================
	public void solveMaster(PrintWriter w, boolean notEndMaster) {
		double[] forti = new double[m];
		for (int j = 0; j < m; j++) {
			forti[j] = s[j] + (r[j] * pr[j]);
		}

		try {

			w.println("===============================");
			w.println("master");
			w.println("===============================");

			System.out.println("===============================");
			System.out.println("master");
			System.out.println("===============================");
			
			
			// Define model
			master = new IloCplex();

			// Array for z
			for (int c = 0; c < configuration.length; c++) {
				z = master.numVarArray(configuration.length, 0, 1);
				// z = master.boolVarArray(configuration.length);
			}

			// Array for y
			for (int j = 0; j < m; j++) {
				y = master.numVarArray(m, 0, 1);
				// y = master.boolVarArray(m);
			}

			// Array for x
			for (int j = 0; j < m; j++) {
				x = master.numVarArray(m, 0, 1);
				// x = master.boolVarArray(m);
			}

			// Array for Qw
			for (int j = 0; j < m; j++) {
				Qw = master.numVarArray(m, 0, Double.MAX_VALUE);
			}

			// Array for Qb
			for (int j = 0; j < m; j++) {
				Qb = master.numVarArray(m, 0, Double.MAX_VALUE);
			}

			// Define the objective of Master
			IloLinearNumExpr objective = master.linearNumExpr();

			double[] summ = new double[configuration.length];

			for (int l = 0; l < configuration.length; l++) {
				for (int i = 0; i < n; i++) {
					int j = configuration[l][1];
					summ[l] = summ[l] + ((configuration[l][i + 2] + configuration[l][i + m + 2]) * d[i][j]);
				}
				objective.addTerm(z[l], summ[l]);
			}

			// IloLinearNumExpr Penalty = master.linearNumExpr();

			for (int j = 0; j < m; j++) {
				objective.addTerm(x[j], -100 * pr[j]);
			}

			// objective.add(Penalty);

			
			
			
			master.addMinimize(objective);

			
			
			// -----------------------------
			// Constraints
			// -----------------------------

			// constraint #1
			for (int j = 0; j < m; j++) {
				IloLinearNumExpr exp1 = master.linearNumExpr();
				for (int l = 0; l < configuration.length; l++) {
					if (configuration[l][1]==j) 
					exp1.addTerm(z[l], 1);
				}
				exp1.addTerm(y[j],-1);
				constraints.add(master.addEq(exp1,0));

			}

			
			
			// constraint #2
			for (int j = 0; j < m; j++) {
				IloLinearNumExpr exp2 = master.linearNumExpr();
				exp2.addTerm(x[j], 1);
				exp2.addTerm(y[j],-1);
				master.addLe(exp2, 0);
			}
			
			// constraint #3
			IloLinearNumExpr exp3 = master.linearNumExpr();
			for (int j = 0; j < m; j++) {
				exp3.addTerm(y[j], 1);
			}
			master.addLe(exp3, p);
		
			// constraint #4
			for (int i = 0; i < n; i++) {
				IloLinearNumExpr exp4 = master.linearNumExpr();
				for (int c = 0; c < configuration.length; c++) {
					exp4.addTerm(z[c], configuration[c][i+2]);
				}
				constraints.add(master.addEq(exp4,1));
			}
			
			// constraint #5
			for (int j = 0; j < m; j++) {
				for (int i = 0; i < n; i++) {
					IloLinearNumExpr exp5 = master.linearNumExpr();
					IloLinearNumExpr exp51 = master.linearNumExpr();
					IloLinearNumExpr exp52 = master.linearNumExpr();

					for (int l = 0; l < configuration.length; l++) {
						if (j == configuration[l][1]) {
								exp51.addTerm(z[l], configuration[l][i + 2]);
							}
							exp52.addTerm(z[l], configuration[l][i+n+2]);
						}
					exp5.add(exp51);
					exp5.add(exp52);
					exp5.addTerm(x[j], 1);
					constraints.add(master.addLe(exp5, 2));
					}
				}
			
			// constraint #6
			for (int j = 0; j < m; j++) {
				for (int i = 0; i < n; i++) {
					IloLinearNumExpr exp6 = master.linearNumExpr();
					IloLinearNumExpr exp61 = master.linearNumExpr();
					IloLinearNumExpr exp62 = master.linearNumExpr();

					for (int l = 0; l < configuration.length; l++) {
						if (j == configuration[l][1]) {
								exp61.addTerm(z[l],-configuration[l][i+2]);
							}
							exp62.addTerm(z[l], configuration[l][i+n+2]);
						}
					exp6.add(exp61);
					exp6.add(exp62);
					exp6.addTerm(x[j], 1);
					constraints.add(master.addGe(exp6, 0));
					}
				}
			/*
			// constraint #7
			for (int i = 0; i < n; i++) {
				IloLinearNumExpr exp7 = master.linearNumExpr();

				for (int j = 0; j < m; j++) {
					for (int l = 0; l < configuration.length; l++) {
						if (j == configuration[l][1]) {
							double sum7= pr[j]*configuration[l][i + 2];
							exp7.addTerm(z[l],sum7);
						}
					}
				}
				
				for (int j = 0; j < m; j++) {
					for (int l = 0; l < configuration.length; l++) {
						if (j == configuration[l][1]) {
							double sum7= - pr[j]*configuration[l][i+n+2];
							exp7.addTerm(z[l],sum7);
						}
					}
				}
				
				for (int l = 0; l < configuration.length; l++) {
					exp7.addTerm(z[l],configuration[l][i+n+2]);
					}
				
				constraints.add(master.addLe(exp7, 1));

			}
			
*/
			// constraint #8
			IloLinearNumExpr exp8 = master.linearNumExpr();
			for (int j = 0; j < m; j++) {
				exp8.addTerm(x[j], forti[j]);
			}
			master.addLe(exp8, B); // n-1+2*(n*m) to n-1+2(n*m)+m constraints


			if (master.solve()) {
				w.println("objective=" + master.getObjValue());
				for (int l = 0; l < configuration.length; l++) {
					if (master.getValue(z[l]) > 0)
						w.println("z[" + l + "]=" + master.getValue(z[l]));
				}
				for (int j = 0; j < m; j++) {
					//if (master.getValue(x[j]) > 0)

				//		w.println("x[" + j + "]=" + master.getValue(x[j]));
				}
				for (int j = 0; j < m; j++) {
					//if (master.getValue(y[j]) > 0)
					//	w.println("y[" + j + "]=" + master.getValue(y[j]));
				}
				/*
				 * for (int j = 0; j < m; j++) { w.println("Qw[" + j + "]=" +
				 * master.getValue(Qw[j])); } for (int j = 0; j < m; j++) {
				 * w.println("Qb[" + j + "]=" + master.getValue(Qb[j])); }
				 */
				// Add Duals
				for (int i = 0; i < constraints.size(); i++) {
					dualConstraints.add(master.getDual(constraints.get(i)));
				}

			}

			// lastMaster = master;
			if (!notEndMaster)
				master.end();

		} catch (IloException exc) {
			exc.printStackTrace();
		}
	}

	public void solveMIP(PrintWriter w) {
		try {

			convertToMIP();
			if (master.solve()) {
				// displaySolution();
				// logger.writeLog(instance, master.getObjValue(),
				// cplex.getBestObjValue());
				w.println("\n\n\n\n\n\nINTEGER VALUES:::::::::::::::::::::");

				w.println("objective=" + master.getObjValue());

				for (int l = 0; l < configuration.length; l++) {
					if (master.getValue(z[l]) > 0)
						w.println("z[" + l + "]=" + master.getValue(z[l]));
				}
				for (int j = 0; j < m; j++) {
					if (master.getValue(x[j]) > 0)
						w.println("x[" + j + "]=" + master.getValue(x[j]));
				}
				for (int j = 0; j < m; j++) {
					if (master.getValue(y[j]) > 0)
						w.println("y[" + j + "]=" + master.getValue(y[j]));
				}

			} else {
				System.out.println("Integer solution not found");
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	public void convertToMIP() {
		// master = new IloCplex();
		// master = lastMaster;
		try {
			mipConversion.add(master.conversion(z, IloNumVarType.Bool));
			master.add(mipConversion.get(mipConversion.size() - 1));
			mipConversion.add(master.conversion(x, IloNumVarType.Bool));
			master.add(mipConversion.get(mipConversion.size() - 1));
			mipConversion.add(master.conversion(y, IloNumVarType.Bool));
			master.add(mipConversion.get(mipConversion.size() - 1));
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}
}
