import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.security.auth.login.Configuration;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

public class InitialSolution<Demand> {

	int n = Parameters.user;
	int m = Parameters.supplier;
	double pr[] = Parameters.pr;
	double B = Parameters.Budget;
	double[] r = Parameters.r;
	double[] s = Parameters.s;
	double[] forti = new double[m];
	double[] Q = Parameters.Q;
	double[][] d = Parameters.distance;
	double[] Demand = Parameters.Demand;
	int p = Parameters.p;
	int[][] aw = new int[n][m];
	int[][] ab = new int[n][m];

	double[] Qw = new double[m];
	double[] Qb = new double[m];
	int[] x = new int[m]; // fortification

	int[] sorted = new int[m];
	int[] opendFacility = new int[p];
	int member = 0;
	double[][] backupDemand = new double[m][m];

	public void InitialCreation() {

		for (int j = 0; j < m; j++) {
			forti[j] = s[j] + (r[j] * pr[j]);
		}

		for (int x = 0; x < opendFacility.length; x++) {
			opendFacility[x] = 1000;
			// Integer.MAX_VALUE;
		}

		int[] fortifiedFacility= new int[p];

		//= {0, 2, 1000, 3, 1};
				
		// ========================== Parimary User-Facility Assignment
		// ===================================

		for (int i = 0; i < n; i++) {
			// System.out.println("i="+i);

			for (int j = 0; j < m - 1; j++) {
				if (d[i][j] > d[i][j + 1]) {
					sorted[j] = j + 1;
					sorted[j + 1] = j;
				} else {
					sorted[j] = j;
					sorted[j + 1] = j + 1;
				}
				// System.out.println("d["+i+"]["+j+"]="+d[i][j]);
				// System.out.println("d["+i+"]["+ k +"]="+d[i][k]);
				// System.out.println("sorted[" + j + "]=" + sorted[j]);
			}

			for (int l = 0; l < sorted.length; l++) {
				int j = sorted[l];
				// System.out.println(sorted[l]);

				// j is already opened or no
				for (int k = 0; k < opendFacility.length; k++) {
					if (opendFacility[k] == j) {
						member = 1;
						// System.out.println(opendFacility[k]);
						// System.out.println(j);
						// System.out.println(member);
						// System.out.println("opendFacility["+k+"]=" +
						// opendFacility[k]);
						break;
					} else {
						member = 0;
						// System.out.println(opendFacility[k]);
						// System.out.println(j);
						// System.out.println(member);
					}

				}

				if (Demand[i] <= (Q[j] - Qw[j])) {

					if (member == 1) {
						aw[i][j] = 1;
						// System.out.println("aw[" + i + "][" + j + "]=" + aw[i][j]);
						Qw[j] = Qw[j] + Demand[i];
						l = sorted.length - 1;

					} else if (member == 0) {
						for (int k = 0; k < opendFacility.length; k++) {
							if (opendFacility[k] == 1000) {
								opendFacility[k] = j;
								 System.out.println("opendFacility["+k+"]=" + opendFacility[k]);
								aw[i][j] = 1;
								// System.out.println("aw[" + i + "][" + j + "]=" + aw[i][j]);
								Qw[j] = Qw[j] + Demand[i];
								l = sorted.length - 1;
								k = opendFacility.length;
							}
						}
					}
				}

			}

		}
		// ==============================================================================================

		// ========================== Facility Fortification
		// ============================================
		// sorting
		fortifiedFacility = opendFacility;
		int temp;
		for (int l = 0; l < fortifiedFacility.length; l++) {
			//int j = fortifiedFacility[l];
			if (fortifiedFacility[l] <= m){
			for (int c = l+1; c < fortifiedFacility.length; c++) {
				//int k = fortifiedFacility[c];
				if (fortifiedFacility[c] <= m && pr[fortifiedFacility[l]] < pr[fortifiedFacility[c]]) {
					temp = fortifiedFacility[l];
					fortifiedFacility[l]= fortifiedFacility[c];
					fortifiedFacility[c] = temp;
					//System.out.println("fortifiedFacility[" + fortifiedFacility[l] + "]=" + fortifiedFacility[fortifiedFacility[l]]);
					//System.out.println("fortifiedFacility[" + fortifiedFacility[c] + "]=" + fortifiedFacility[fortifiedFacility[c]]);
				}
			}
			}
			
		}
		
		for (int k = 0; k < fortifiedFacility.length; k++) {
			//System.out.println("fortifiedFacility[" + k + "]=" + fortifiedFacility[k]);
			
		}


		// fortification
		double cost = 0;
		for (int k = 0; k < fortifiedFacility.length; k++) {
			int j = fortifiedFacility[k];
			if (j<=n) {
			cost = cost + forti[j];
			// System.out.println("fortifi[" + j + "]=" +forti[j]);
			if (cost <= B) {
				x[j] = 1;
				 System.out.println("x[" + j + "]=" +x[j]);
			}
			}
		}
		
		
//		===================================== list corection for those ones that are not in the list of opened facility ==============
		int temp1 = 1000;
		for (int i = 0; i < fortifiedFacility.length; i++) {
			if (fortifiedFacility[i]>m){
				for (int j = 0; j < sorted.length; j++) {
					for (int k = 0; k < fortifiedFacility.length ; k++) {
						if (sorted[j]!= fortifiedFacility[k]) {
							temp1=sorted[j];
						}else if (sorted[j]!= fortifiedFacility[k]) {
							temp=1000;	
						}
					}
					
				}
				fortifiedFacility[i]= temp1;
			}
			//System.out.println("=========================");
			//System.out.println("fortifiedFacility[" + i + "]=" + fortifiedFacility[i]);
			
		}
		
		
		
		// ========================== Backup User-Facility Assignment
		// ===================================
		int cuting=0;
		for (int j = 0; j < m; j++) {
			Qb[j] = Q[j] - Qw[j];
		}

		for (int i = 0; i < n; i++) {
			//System.out.println(i);
			for (int k = 0; k < fortifiedFacility.length; k++) {
				int j = fortifiedFacility[k];
				
				
				for (int j1 = 0; j1 < m; j1++) {
					if (ab[i][j1]==1) {
						k = fortifiedFacility.length;
						
					}
				}
			
				//System.out.println(j);
				if ( k < fortifiedFacility.length && aw[i][j] == 1 && x[j] != 1) {
					for (int l = 0; l < fortifiedFacility.length; l++) {
						int j1 = fortifiedFacility[l];
						if (j != j1) {
							backupDemand[j][j1] = backupDemand[j][j1] + Demand[i];
							if (backupDemand[j][j1] <= Qb[j1]){
								 ab[i][j1] = 1;
								 l = fortifiedFacility.length;
							}
						
							
							else backupDemand[j][j1] = backupDemand[j][j1] - Demand[i];
							//System.out.println("ab[" + i + "][" + j1 + "]=" + ab[i][j1]);
						}
					}
				}
			}
		}
		
		// ============================= Creating configuration ============================
		int[][] configuration = new int [n][2+2*m];
		
		for (int i = 0; i < n; i++) {
			configuration[i][0]=i;
			configuration[i][1]=i;
			System.out.print("{"+configuration[i][0]+","+configuration[i][1]+",");
			for (int j = 0; j < m; j++) {
				if (aw[i][j]==1) configuration [i][j+2]=1;
				else configuration[i][j+2]=0;
				System.out.print(aw[i][j]+",");
			}
			
			for (int j = 0; j < m; j++) {
				if (ab[i][j]==1)configuration [i][j+2+m]=1;
				else configuration[i][j+2+m]=0;
				if (j==m-1) System.out.print(ab[i][j]+"},");
					
				else
				System.out.print(ab[i][j]+",");
			}
				
			System.out.println();
			
		}
	
		
		
		
//		=======================================================
		//System.out.println("aw=");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				//System.out.print(aw[i][j]+",");
				
				
			}
			//System.out.println();
			
		}
		//System.out.println("ab=");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				//System.out.print(ab[i][j]+",");
				
				
			}
			//System.out.println();
			
		}
	}
}
/*
{ 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0 },
*/
