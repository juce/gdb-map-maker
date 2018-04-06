package editor;
import java.io.*;

public class Main
{ 
	static OptionFile of = new OptionFile();
	
	public static void main(String [] args) throws Exception
	{
		of.readXPS(new File(args[0]));
        System.out.write(of.data);
	}
}
