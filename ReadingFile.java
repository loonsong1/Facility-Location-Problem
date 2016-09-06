import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class ReadingFile {
	
	
	public ReadingFile(){
		
		
		ArrayList a1 = new ArrayList();
		ArrayList a2 = new ArrayList();
		ArrayList a3 = new ArrayList();
	

	
    // The name of the file to open.
    String fileName = "User88.txt";
//    String fileName2 = "Supplier.txt";


    // This will reference one line at a time
    String line = null;

    try {
        // FileReader reads text files in the default encoding.
        FileReader fileReader = new FileReader(fileName);

        // Always wrap FileReader in BufferedReader.
        BufferedReader bufferedReader = new BufferedReader(fileReader);

		System.out.println("The facility and users");
        while((line = bufferedReader.readLine()) != null) {
        	
			String[] splits = line.split(",");
			double i = Double.parseDouble(splits[0]);
			double xi = Double.parseDouble(splits[1]);
			double yi= Double.parseDouble(splits[2]);
			a1.add(i);
			a2.add(xi);
			a3.add(yi);			
			System.out.println(i + ",	" + xi + ",	"+ yi);
			
        } 
		System.out.println("List of users:"+a1 );
		System.out.println("X of users:" + a2 );
		System.out.println("Y of users:" + a3);
		System.out.println("-----------------------------");
		
        // Always close files.
        bufferedReader.close();         
    }
    catch(FileNotFoundException ex) {
        System.out.println(
            "Unable to open file '" + 
            fileName + "'");                
    }
    catch(IOException ex) {
        System.out.println(
            "Error reading file '" 
            + fileName + "'");                  

    }
    	System.out.println("Distances:");
    	
    	
		for(int i=0;i <=a1.size()-1;i++){
			System.out.print( "{");

			for(int j=0;j <=a1.size()-1;j++){	
				if(i!=j){
						double x1= (Double) a1.get(i);
						double x2= (Double) a1.get(j);
						double x3=Math.abs(x2- x1);
						double y1= (Double) a2.get(i);
						double y2= (Double) a2.get(j);
						double y3=Math.abs(x2- x1);			
						double distance = Math.sqrt( (x3*x3) + (y3*y3) );
						DecimalFormat df = new DecimalFormat("###.##");
						int i1=i+1;
						int j1=j+1;	
						if (j<a1.size()-1) 	
							System.out.print(df.format(distance)+ ",");
							//System.out.print( distance+ ",");	
						
						else System.out.print( df.format(distance));	
						

					
						}
				else if(i==j){
					
					System.out.print(0 + ",");
					
				}
					}
			System.out.println( "},");

				}
		System.out.println( "};");
	}
}