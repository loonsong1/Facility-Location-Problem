import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloConversion;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

public class MasterMIP {

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
	// double [] forti = Parameters.forti ; //fortification term

	List<IloRange> constraints = new ArrayList<IloRange>();
	List<Double> dualConstraints = new ArrayList<Double>();
	// List<IloConversion> MIP = new ArrayList<IloConversion>();

	IloCplex mastermip;

	// ==============================================
	public void solveMasterMIP(PrintWriter w) {

		double[] forti = new double[m];
		for (int j = 0; j < m; j++) {
			forti[j] = s[j] + (r[j] * pr[j]);
		}

		int[][] configuration = Parameters.configuration;

		try {

			w.println("===============================");
			w.println("master");
			w.println("===============================");

			// Define model
			mastermip = new IloCplex();
			
			
			// Decision variable
			IloNumVar[] z = new IloNumVar[configuration.length];
			IloNumVar[] y = new IloNumVar[m];
			IloNumVar[] x = new IloNumVar[m];
			IloNumVar[] Qw = new IloNumVar[m];
			IloNumVar[] Qb = new IloNumVar[m];

			// Array for z
			for (int c = 0; c < configuration.length; c++) {
				//z = mastermip.numVarArray(configuration.length, 0, 1);
				 z = mastermip.boolVarArray(configuration.length);
			}

			// Array for y
			for (int j = 0; j < m; j++) {
				//y = mastermip.numVarArray(m, 0, 1);
				 y = mastermip.boolVarArray(m);
			}

			// Array for x
			for (int j = 0; j < m; j++) {
				//x = mastermip.numVarArray(m, 0, 1);
				 x = mastermip.boolVarArray(m);
			}

			// Array for Qw
			for (int j = 0; j < m; j++) {
				Qw = mastermip.numVarArray(m, 0, Double.MAX_VALUE);
			}

			// Array for Qb
			for (int j = 0; j < m; j++) {
				Qb = mastermip.numVarArray(m, 0, Double.MAX_VALUE);
			}

			// Define the objective of Master
			IloLinearNumExpr objective = mastermip.linearNumExpr();
			double[] summ = new double[configuration.length];

			for (int l = 0; l < configuration.length; l++) {
				for (int j = 0; j < m; j++) {
					int i = configuration[l][1];
					summ[l] = summ[l] + ((configuration[l][j + 2] + configuration[l][j + m + 2]) * d[i][j]);
					//System.out.println(summ[l]);
				}
				objective.addTerm(z[l], summ[l]);
			}
			mastermip.addMinimize(objective);

			// -----------------------------
			// Constraints
			// -----------------------------

			// constraint #1
			for (int i = 0; i < n; i++) {
				IloLinearNumExpr exp1 = mastermip.linearNumExpr();
				for (int l = 0; l < configuration.length; l++) {
					if (i == configuration[l][1]) {
						for (int j = 0; j < m; j++) {
							exp1.addTerm(z[l], configuration[l][j + 2]);
						}
					}
				}
				constraints.add(mastermip.addEq(exp1, 1));

			}

			// constraint #2
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					IloLinearNumExpr exp2 = mastermip.linearNumExpr();
					IloLinearNumExpr exp21 = mastermip.linearNumExpr();
					IloLinearNumExpr exp22 = mastermip.linearNumExpr();

					for (int l = 0; l < configuration.length; l++) {
						if (i == configuration[l][1]) {
							for (int k = 0; k < m; k++) {
								exp21.addTerm(z[l], configuration[l][k + m + 2]);
							}
							exp22.addTerm(z[l], configuration[l][j + 2]);
						}
					}
					exp2.add(exp21);
					exp2.add(exp22);
					exp2.addTerm(x[j], 1);
					constraints.add(mastermip.addLe(exp2, 2));
				}
			}

			// constraint #3
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					IloLinearNumExpr exp3 = mastermip.linearNumExpr();
					IloLinearNumExpr exp31 = mastermip.linearNumExpr();
					IloLinearNumExpr exp32 = mastermip.linearNumExpr();

					for (int l = 0; l < configuration.length; l++) {
						if (i == configuration[l][1]) {
							for (int k = 0; k < m; k++) {
								exp31.addTerm(z[l], configuration[l][k + m + 2]);
							}
							exp32.addTerm(z[l], -configuration[l][j + 2]);
						}
					}
					exp3.add(exp31);
					exp3.add(exp32);
					exp3.addTerm(x[j], 1);
					constraints.add(mastermip.addGe(exp3, 0));
				}
			}

			// constraint #4
			for (int j = 0; j < m; j++) {
				IloLinearNumExpr exp4 = mastermip.linearNumExpr();
				exp4.addTerm(x[j], 1);
				exp4.addTerm(y[j], -1);
				mastermip.addLe(exp4, 0);
			}

			// constraint #5
			IloLinearNumExpr exp5 = mastermip.linearNumExpr();
			for (int j = 0; j < m; j++) {
				exp5.addTerm(x[j], forti[j]);
			}
			mastermip.addLe(exp5, B); // n-1+2*(n*m) to n-1+2(n*m)+m constraints

			// constraint #6
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					IloLinearNumExpr exp6 = mastermip.linearNumExpr();

					for (int l = 0; l < configuration.length; l++) {
						if (i == configuration[l][1]) {
							int sum = configuration[l][j + 2] + configuration[l][j + m + 2];
							exp6.addTerm(z[l], sum); // sum (aw[j][c]+ab[j][c])
														// z[c] on ci & j
							exp6.addTerm(y[j], -100);
							constraints.add(mastermip.addLe(exp6, 0));
						}
					}
				}
			}

			// constraint #7
			IloLinearNumExpr exp7 = mastermip.linearNumExpr();
			for (int j = 0; j < m; j++) {
				exp7.addTerm(y[j], 1);
			}
			mastermip.addLe(exp7, p);

			// constraint #8
			for (int j = 0; j < m; j++) {
				IloLinearNumExpr exp8 = mastermip.linearNumExpr();
				// IloLinearNumExpr exp81 = master.linearNumExpr();

				for (int l = 0; l < configuration.length; l++) {
					int i = configuration[l][1];
					double exp811 = D[i] * configuration[l][j + 2];
					exp8.addTerm(z[l], exp811);

				}
				// exp8.add(exp81);
				exp8.addTerm(Qw[j], -1);
				constraints.add(mastermip.addLe(exp8, 0));
			}

			// constraint #9
			for (int j1 = 0; j1 < m; j1++) {
				for (int j2 = 0; j2 < m; j2++) {
					IloLinearNumExpr exp9 = mastermip.linearNumExpr();
					for (int i = 0; i < n; i++) {
						for (int l = 0; l < configuration.length; l++) {
							if (i == configuration[l][1]) {
								if (j2 != j1) {
									double exp911 = D[i] * configuration[l][j2 + 2] * configuration[l][j1 + m + 2];
									exp9.addTerm(z[l], exp911);
								}
								exp9.addTerm(Qb[j1], -1);
							}
						}
					}
					constraints.add(mastermip.addLe(exp9, 0));
				}
			}

			// constraint #10
			for (int j = 0; j < m; j++) {
				IloLinearNumExpr exp10 = mastermip.linearNumExpr();

				exp10.addTerm(Qw[j], 1);
				exp10.addTerm(Qb[j], 1);
				mastermip.addLe(exp10, Q[j]);
			}

			if (mastermip.solve()) {
				w.println("*****************************");
				w.println("objective=" + mastermip.getObjValue());

				for (int l = 0; l < configuration.length; l++) {
					if (mastermip.getValue(z[l]) > 0)
						w.println("z[" + l + "]=" + mastermip.getValue(z[l]));
				}
				for (int j = 0; j < m; j++) {
					w.println("x[" + j + "]=" + mastermip.getValue(x[j]));
				}
				for (int j = 0; j < m; j++) {
					w.println("y[" + j + "]=" + mastermip.getValue(y[j]));
				}
/*
				for (int j = 0; j < m; j++) {
					w.println("Qw[" + j + "]=" + mastermip.getValue(Qw[j]));
				}
				for (int j = 0; j < m; j++) {
					w.println("Qb[" + j + "]=" + mastermip.getValue(Qb[j]));
				}
*/
			
			}

			mastermip.end();

		} catch (IloException exc) {
			exc.printStackTrace();
		}
	}
}
