import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class DLLTest {
	
	CLibrary cl;

	/**
	 * 
	 * 
	 * setDectector(int type)
	 * 0 - BRISK
	 * 1 - AKAZE -- Note ** (AKAZE descriptors can only be used with KAZE or AKAZE keypoints)
	 * 2 - ORB -- Note ** (ORB descriptors must be converted to CV_32F in order to work with FLANN)
	 * 3 - SURF //not yet implemented
	 * 4 - SIFT //not yet implemented
	 * 5 - FREAK
	 * 
	 * setMatcher(int type)
	 * 0 - BruteForce
	 * 1 - FLANN
	 * 
	 * 
	 * 
	 * Use: 
	 *  - Set your detector
	 *  - Set your matcher
	 *  - Set your subject image (The base image to search for in other images);
	 *  - Detect Keypoints and Descriptors for Subject Image
	 *  
	 *  
	 */
	public interface CLibrary extends Library {
		boolean setSubjectImage(byte[] p, int height, int width);
		boolean checkSubject();
		boolean setDetector(int type); //if not called Default is BRISK
		boolean setMatcher(int type); //if not called Default is BRUTE FORCE W/HAMMING
		boolean setSubjectKeypoints();
		boolean compareToSubject(byte[] p, int h1, int w1);
		void cleanUp();
	}
	
	public DLLTest(){
		Native.setProtected(true);
		cl = (CLibrary) Native.loadLibrary("C:\\Users\\Miguel Gallegos\\Documents\\Visual Studio 2015\\Projects\\OcvDLL\\x64\\Release\\OcvDLL", CLibrary.class);
		cl.setDetector(2);
		cl.setMatcher(0);			
	}
	
	public void run(){
       // String searchObject = "C:\\Users\\Miguel Gallegos\\Documents\\TestImages\\bottle1_doubled.jpg";
       // String searchScene = "C:\\Users\\Miguel Gallegos\\Documents\\TestImages\\bottles_doubled.jpg";
       String searchObject = "C:\\Users\\Miguel Gallegos\\Desktop\\Alexie\\ipod\\base.jpg";
       String searchScene = "C:\\Users\\Miguel Gallegos\\Desktop\\Alexie\\ipod\\117_0119.jpg";
			
        try {
        	
        	byte[] buf1, buf2;
        	
        	/* Load object we are searching for */
			BufferedImage img = ImageIO.read(new File(searchObject));
			int h1;
			int w1;			
			int iType1 = img.getType();
			
			if((img.getWidth() * img.getHeight())/1024 < 300){
				img = scaleUp(img);
			}
			
			if(iType1 != 5 && iType1 != 10 && iType1 != 4 && iType1 != 1) {
				BufferedImage iBGR1;
				System.out.println("Converting to BGR");
				iBGR1 = convertToBGR(img);
				img.flush();
				buf1 = ((DataBufferByte) iBGR1.getRaster().getDataBuffer()).getData();
				h1 = iBGR1.getHeight();
				w1 = iBGR1.getWidth();
			} else {
				buf1 = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
				h1 = img.getHeight();
				w1 = img.getWidth();
			}  
			
			
			//set the subject
			cl.setSubjectImage(buf1, h1, w1);
			
			if(cl.checkSubject()){
				cl.setSubjectKeypoints();
			} else {
				return;
			}
			
			int h2;
			int w2;
        	
            System.out.println(searchScene);
			BufferedImage img1 =ImageIO.read(new File(searchScene));
			
			int size = (img1.getHeight() * img1.getWidth())/1024;

			iType1 = img1.getType();
			if(iType1 != 5 && iType1 != 10 && iType1 != 4 && iType1 != 1){
				System.out.println("Converting to BGR");
				BufferedImage iBGR2;
				iBGR2 = convertToBGR(img1);
				buf2 = ((DataBufferByte) iBGR2.getRaster().getDataBuffer()).getData();
				h2 = iBGR2.getHeight();
				w2  = iBGR2.getWidth();				
			} else {
				buf2 = ((DataBufferByte) img1.getRaster().getDataBuffer()).getData();
				h2 = img1.getHeight();
				w2  = img1.getWidth();	
			}
			
			//long startTime = System.currentTimeMillis();  
			cl.compareToSubject(buf2, h2, w2);
			//System.out.println("Comparison: " +  (System.currentTimeMillis() - startTime) + "ms");			     				        			
			
			//img1.flush();
			//buf2 = null;			

			
        } catch(Exception e){
        	e.printStackTrace();
        }
	}
	
	private BufferedImage convertToBGR(BufferedImage img) throws IOException{
		int size = (img.getHeight() * img.getWidth())/1024;
		BufferedImage img1;

		img1 = new BufferedImage(img.getWidth(), img.getHeight(),  BufferedImage.TYPE_3BYTE_BGR); 

		
		Graphics2D g2d= img1.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();	

		return img;
		//File ouptut = new File("C:\\Users\\299490\\Desktop\\Alexie\\bgr.jpg");
		//ImageIO.write(img1, "jpg", ouptut);
		
	}
		
	private BufferedImage scaleUp(BufferedImage img){
		int width = img.getWidth() *2;
		int height = img.getHeight() *2;
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

		Graphics2D g = newImage.createGraphics();
		//g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, width, height, null);
		g.dispose();		
		return newImage;
		
	}
	
	public static void main(String[] args) {
		DLLTest t = new DLLTest();
		t.run();

	}
}
